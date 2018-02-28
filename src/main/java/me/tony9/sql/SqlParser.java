package me.tony9.sql;

import me.tony9.util.tree.Node;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.*;
import java.util.stream.Collectors;

public class SqlParser {

    private static Log logger = LogFactory.getLog(SqlParser.class);

    private final static Set<String> KEYWORDS;
    static {
        String[] statements = new String[] {
                //DDL
                "CREATE TABLE",
                "CREATE TABLE WITH DATA",
                "CREATE INDEX ON",
                "DROP TABLE",
                "DROP INDEX",
                //DML
                "SELECT FROM [LEFT|RIGHT|INNER|OUTER] JOIN ON WHERE GROUP BY HAVING ORDER BY LIMIT",
                "SELECT UNION [ALL] SELECT",
                "WITH SELECT FROM SELECT FROM SELECT FROM",
                "INSERT INTO VALUES",
                "INSERT INTO SELECT FROM",
                "UPDATE SET WHERE",
                "DELETE FROM WHERE",
        };

        KEYWORDS = new HashSet<>();
        for (int i = 0; i < statements.length; i ++) {
            for (String w : statements[i].split("[\\s\\[\\]\\|]+")) {
                KEYWORDS.add(w);
            }
        }
    }


    private final static Set<String> MULTIPLE_KEYWORDS; //合法的合并关键字
    static {
        String[] multipleKeywords = new String[] {
                //DDL
                "CREATE-TABLE WITH-DATA",
                "CREATE-INDEX",
                "DROP-TABLE",
                "DROP-INDEX",
                //DML
                "LEFT-JOIN RIGHT-JOIN INNER-JOIN OUTER-JOIN",
                "GROUP-BY ORDER-BY UNION-ALL",
                "INSERT-INTO",
        };

        MULTIPLE_KEYWORDS = new HashSet<>();
        for (int i = 0; i < multipleKeywords.length; i ++) {
            for (String w : multipleKeywords[i].split("[\\s\\[\\]\\|]+")) {
                MULTIPLE_KEYWORDS.add(w);
            }
        }
    }

    private static class SqlNode extends Node<SqlNode> {
        private String text;

        public SqlNode() {
            this.text = "*NONE";
        }
        public SqlNode(String text) {
            this.text = text;
        }

        public String toString() {
            return this.text;
        }
    }

    private static class Token extends SqlNode {

        private List<SqlToken> sqlTokens;
        private boolean keyword;

        public Token(Token t) {
            this.sqlTokens = t.sqlTokens;
            this.keyword = t.keyword;
        }

        public Token(SqlToken sqlToken, boolean keyword) {
            this.sqlTokens = new ArrayList<>();
            this.sqlTokens.add(sqlToken);
            this.keyword = keyword;
        }

        public Token(List<SqlToken> sqlTokens, boolean keyword) {
            this.sqlTokens = sqlTokens;
            this.keyword = keyword;
        }

        public String toString() {
            StringBuffer str = new StringBuffer();
            for (int i = 0; i < this.sqlTokens.size(); i ++) {
                if (i > 0) str.append("-");
                str.append(this.sqlTokens.get(i).getText());
            }
            return this.keyword? "`"+str.toString().toUpperCase()+"`":str.toString();
        }

        public boolean canAppend(Token t) {

            if (!this.keyword || !t.keyword) return false;

            StringBuffer str = new StringBuffer();
            for (int i = 0; i < this.sqlTokens.size(); i ++) {
                if (i > 0) str.append("-");
                str.append(this.sqlTokens.get(i).getText());
            }
            str.append("-");
            for (int i = 0; i < t.sqlTokens.size(); i ++) {
                if (i > 0) str.append("-");
                str.append(t.sqlTokens.get(i).getText());
            }

            return MULTIPLE_KEYWORDS.contains(str.toString().toUpperCase());

        }

        public void append(Token t) {
            this.sqlTokens.addAll(t.sqlTokens);
        }
    }

    private static class Expression extends SqlNode {

        private List<SqlToken> sqlTokens;

        public Expression(List<SqlToken> sqlTokens) {
            super();
            this.sqlTokens = sqlTokens;
        }

        public String toString() {

            StringBuffer str = new StringBuffer();
            for (int i = 0; i < this.sqlTokens.size(); i ++) {
                if (i > 0) str.append(" ");
                str.append(this.sqlTokens.get(i).getText());
            }
            return str.toString();
        }
    }

    private static class Statement extends SqlNode {

        public Statement() {

            super("`STATEMENT`");
        }

        protected Statement(String text) {

            super(text);
        }
    }

    private static class UnionStatement extends Statement {

        public UnionStatement() {

            super("`UNION-STATEMENT`");
        }
    }


    public Node parse(String sql) {
        SqlTokenizer sqlTokenizer = new SqlTokenizer();
        List<SqlToken> tokens = sqlTokenizer.split(sql);
        return SqlASTBuilder.buildSimpleAST(tokens);
    }


    private static class SqlASTBuilder {

        private static String TOKEN_LEFT_PAREN = "(";
        private static String TOKEN_RIGHT_PAREN = ")";
        private static String TOKEN_COMMA = ",";
        private static String TOKEN_UNION = "`UNION`";
        private static String TOKEN_ALL = "`ALL`";

        private static String TOKEN_SELECT = "`SELECT`";


        private static class Interval<T> {
            private T start;
            private T stop;

            public Interval(T start, T stop) {
                this.start = start;
                this.stop = stop;
            }

            @Override
            public String toString() {
                return "(" + start + ", " + stop + ")";
            }

        }

        private static List<Interval<Integer>> findAllParenPairs(List<Token> tokens) {

            List<Interval<Integer>> intervals = new ArrayList<>();

            LinkedList<Integer> stack = new LinkedList<>();

            for (int i = 0; i < tokens.size(); i++) {

                Token token = tokens.get(i);
                if (token.toString().equals(TOKEN_LEFT_PAREN)) {

                    stack.push(i);

                } else if (token.toString().equals(TOKEN_RIGHT_PAREN)) {

                    Integer start = stack.pop();
                    Integer stop = i;
                    intervals.add(new Interval<>(start, stop));
                }
            }

            return intervals;
        }


        /**
         * 分析嵌套子句，组装Statement对象
         * 包括以下情形
         * 1. 子句在括弧中
         *  a. select from (select from )
         *  b. select from join (select from )
         *  c. create table t as (select from )
         *  d. insert into t (select from )
         * 2. 子句不在括弧中
         *  a. union 语句不含括弧: select from union [all] select from
         *  b. with-select 最后一句子句没有括弧: with a as (select from), b as (select from) select from
         *  c. insert-into 最后一句子句可不加括弧: insert into ... select，
         *
         * 基本算法
         *
         * @param tokens
         * @return
         */
        private static Statement buildNestedStatement(List<Token> tokens) {

            //计算Union/Union-All 位置
            List<Token> newTokens = new ArrayList<>();
            List<Integer> unionNodePos = new ArrayList<>();
            int parenCount = 0;
            for (int i = 0; i < tokens.size(); i ++) {
                Token t = tokens.get(i);
                String s = t.toString();
                if (TOKEN_LEFT_PAREN.equals(s)) {
                    parenCount ++;
                } else if (TOKEN_RIGHT_PAREN.equals(s)) {
                    parenCount --;
                }

                if (parenCount == 0) {
                    if (TOKEN_UNION.equals(s)) {
                        unionNodePos.add(i);
                        if (TOKEN_ALL.equals(tokens.get(i+1).toString())) {
                            t.append(tokens.get(i+1));
                            newTokens.add(t);
                            i += 1;
                        } else {
                            newTokens.add(t);
                        }
                    } else {
                        newTokens.add(t);
                    }
                } else {
                    newTokens.add(t);
                }
            }

            //return
            if (unionNodePos.size() > 0) {
                return buildNestedStatementWithUnion(newTokens, unionNodePos);
            } else {
                return buildNestedStatementWithoutUnion(newTokens);
            }
        }

        private static Statement buildNestedStatementWithUnion(List<Token> tokens, List<Integer> unionNodePos) {

            Statement statement = new UnionStatement();
            List<Integer> pos = new ArrayList<>();
            pos.add(-1);
            pos.addAll(unionNodePos);
            pos.add(tokens.size());

            for (int i = 0; i < pos.size()-1; i ++) {
                int start = pos.get(i)+1;
                if (TOKEN_LEFT_PAREN.equals(tokens.get(start).toString())) start ++;
                int end = pos.get(i+1);
                if (end-1 < tokens.size() && TOKEN_RIGHT_PAREN.equals(tokens.get(end-1).toString())) end --;

                List<Token> subTokens = new ArrayList<>();
                for (int k = start; k < end; k ++) subTokens.add(tokens.get(k));

                statement.addLast(buildNestedStatement(subTokens));
                if (i < pos.size()-2) statement.addLast(tokens.get(pos.get(i+1)));
            }

            return rebuildLastSelectStatement(statement);
        }

        /**
         *
         *
         * 处理嵌套的SQL子句。
         * 要求：嵌套SQL必须在括弧中
         *
         * 比如
         * 1. select from (select from (select from ...))
         * 2. create table t as (select from ...)
         * 3. insert into t (select from ...)
         *
         * @param tokens
         * @return
         */
        private static Statement buildNestedStatementWithoutUnion(List<Token> tokens) {

            //
            //找到第一层嵌套语句
            //

            //找到所有select子句 （注意, 所有的子句都是select语句）
            List<Interval<Integer>> allSubQueryPairs = findAllParenPairs(tokens).stream()
                    .filter(interval -> TOKEN_SELECT.equals(tokens.get(interval.start + 1).toString()))
                    .collect(Collectors.toList());

            //找到最外层的select子句
            List<Interval<Integer>> topSubQuery = allSubQueryPairs.stream()
                    .filter(interval -> !allSubQueryPairs.stream().anyMatch(p -> p.start < interval.start && p.stop > interval.stop))
                    .collect(Collectors.toList());

            Map<Integer, Interval<Integer>> map = new HashMap<>();
            for (Interval<Integer> interval: topSubQuery) {
                map.put(interval.start, interval);
            }

            //
            // 递归执行
            //
            Statement statement = new Statement();

            int parenCount = 0;
            for (int i = 0; i < tokens.size(); i ++) {
                Token t = tokens.get(i);

                if (map.containsKey(i)) {
                    Interval<Integer> interval = map.get(i);
                    List<Token> newTokens = new ArrayList<>();
                    for (int k = i+1; k < interval.stop; k ++) newTokens.add(tokens.get(k));
                    statement.addLast(buildNestedStatement(newTokens));

                    i = interval.stop;
                } else {
                    statement.addLast(t);
                }
            }

            return rebuildLastSelectStatement(statement);
        }

        /**
         *
         * 下面场景中，结尾处的select子句前后是可以不带括弧的
         * 1. with..select
         * 2. insert into..select
         * 3. select union [all] select
         *
         *
         * @param s
         * @return
         */
        private static Statement rebuildLastSelectStatement(Statement s) {

            LinkedList<Node<SqlNode>> nodes = s.getChildren();
            int lastSelectIndex = nodes.size()-1;
            for (; lastSelectIndex > -1; lastSelectIndex --) {
                if ("`SELECT`".equals(nodes.get(lastSelectIndex).toString())) {
                    break;
                }
            }

            if (lastSelectIndex > 0) {
                Statement newStatement = new Statement();
                for (int i = 0; i < lastSelectIndex; i ++) {
                    newStatement.addLast(nodes.get(i));
                }

                Statement lastSelectStatement = new Statement();
                for (int i = lastSelectIndex; i < nodes.size(); i ++) {
                    lastSelectStatement.addLast(nodes.get(i));
                }
                newStatement.addLast(lastSelectStatement);

                return newStatement;
            } else {
                return s;
            }
        }

        /**
         *
         * @param unionStatement
         * @return
         */
        private static Statement buildUnionStatementNode(UnionStatement unionStatement) {

            LinkedList<Node<SqlNode>> children = unionStatement.getChildren();

            //stack: A1 u1 A2 u2 A3 u3 A4
            //每次出栈三个元素 X u Y，将结果 X u Y 放回栈
            LinkedList<Node<SqlNode>> stack = new LinkedList<>();
            for (Node<SqlNode> n: children) {
                stack.add(n);
            }
            while (stack.size() >= 3) {
                Node s1 = stack.pop();
                if (s1 instanceof Statement) s1 = buildNodes((Statement)s1);

                Node n = stack.pop();

                Statement s2 = (Statement)stack.pop();
                if (s2 instanceof Statement) s2 = buildNodes((Statement)s2);

                n.addLast(s1);
                n.addLast(s2);

                stack.push(n);
            }

            Statement newStatement = new Statement();

            newStatement.addLast(stack.pop());


            return newStatement;
        }

        /**
         *
         * @param nestedStatement
         * @return
         */
        private static Statement buildNotUnionStatementNode(Statement nestedStatement) {

            Statement newStatement = new Statement();

            LinkedList<SqlNode> nodes = new LinkedList<>();
            Token curKeywordToken = null;

            for (int i = 0; i < nestedStatement.getChildren().size(); i ++) {

                SqlNode node = (SqlNode)nestedStatement.getChildren().get(i);

                if (node instanceof Token && ((Token) node).keyword) {

                    Token t = (Token) node;
                    if (curKeywordToken != null) {

                        if (nodes.size() > 0 || !curKeywordToken.canAppend(t)) {
                            buildNode(newStatement, curKeywordToken, nodes);

                            //reset nodes and curKeywordToken
                            nodes = new LinkedList<>();
                            curKeywordToken = new Token(t);

                        } else {    //合并连续的keywords
                            curKeywordToken.append(t);
                        }


                    } else {
                        curKeywordToken = new Token(t);
                    }

                } else if (node instanceof Statement) {
                    Statement subStatement = buildNodes((Statement)node); //递归
                    nodes.add(subStatement);
                } else {
                    nodes.add(node);
                }
            }

            //add remain all
            if (curKeywordToken != null) {

                buildNode(newStatement, curKeywordToken, nodes);
            }

            return newStatement;
        }

        /**
         * 遍历指定statement对象的子节点
         * 1. 把SQL关键字后面的节点调整为当前关键字的子节点
         * 2. 合并同级Token之间的字符
         *
         * 注意，必须传入Nested Statement。
         *
         * @param nestedStatement
         * @return
         */
        private static Statement buildNodes(Statement nestedStatement) {


            if (nestedStatement instanceof UnionStatement) {
                return buildUnionStatementNode((UnionStatement) nestedStatement);
            } else {
                return buildNotUnionStatementNode(nestedStatement);
            }

        }

        private static void buildNode(Statement statement, Token curKeywordToken, List<SqlNode> nodeList) {

            Token curToken;
            String keyword = curKeywordToken.toString();

            if ("`CREATE-TABLE`".equals(keyword)) {         ///---CREATE

                curToken = buildCreateTableNode(curKeywordToken, nodeList);
                statement.addLast(curToken);

            } else if ("`WITH-DATA`".equals(keyword)) {

                statement.addLast(curKeywordToken);

            } else if ("`INSERT-INTO`".equals(keyword)) {   ///---INSERT

                curToken = buildInsertIntoNode(curKeywordToken, nodeList);
                statement.addLast(curToken);

            } else if ("`UPDATE`".equals(keyword)) {        ///---UPDATE

                curToken = buildUpdateNode(curKeywordToken, nodeList);
                statement.addLast(curToken);

            } else if ("`SET`".equals(keyword)) {

                curToken = buildSetNode(curKeywordToken, nodeList);
                statement.addLast(curToken);

            } else if ("`DELETE`".equals(keyword)) {        ///---DELETE

                curToken = buildDeleteNode(curKeywordToken, nodeList);
                statement.addLast(curToken);

            } else if ("`SELECT`".equals(keyword)) {        ///---SELECT

                curToken = buildColumnNode(curKeywordToken, nodeList);
                statement.addLast(curToken);

            } else if ("`WITH`".equals(keyword)) {

                curToken = buildWithNode(curKeywordToken, nodeList);
                statement.addLast(curToken);

            } else if ("`FROM`".equals(keyword)) {

                curToken = buildTableNode(curKeywordToken, nodeList);
                statement.addLast(curToken);

            } else if (keyword.endsWith("-JOIN`")) {

                curToken = buildTableNode(curKeywordToken, nodeList);
                statement.addLast(curToken);

            } else if ("`ON`".equals(keyword)) {

                curToken = buildConditionNode(curKeywordToken, nodeList);
                //把`ON`节点移到之前的`-JOIN`节点，作为`-JOIN`的最后一个子节点
                LinkedList<Node<SqlNode>> nodes = statement.getChildren();
                nodes.get(nodes.size() - 1).addLast(curToken);

            } else if ("`WHERE`".equals(keyword)) {

                curToken = buildConditionNode(curKeywordToken, nodeList);
                statement.addLast(curToken);

            } else if ("`GROUP-BY`".equals(keyword)) {

                curToken = buildGroupByNode(curKeywordToken, nodeList);
                statement.addLast(curToken);

            } else if ("`HAVING`".equals(keyword)) {

                curToken = buildConditionNode(curKeywordToken, nodeList);
                statement.addLast(curToken);

            } else if ("`ORDER-BY`".equals(keyword)) {

                curToken = buildOrderByNode(curKeywordToken, nodeList);
                statement.addLast(curToken);

            } else if ("`LIMIT`".equals(keyword)) {

                //TODO: 分页尚未支持，不同数据库的语句差别较大

            } else if ("`UNION`".equals(keyword) || "`UNION-ALL`".equals(keyword)) {

                curToken = buildUnionNode(curKeywordToken, nodeList);
                statement.addLast(curToken);
                //TODO: 尚未支持

            } else {

                //TODO: 这里异常可以取消，只记录warning日志

                String msg = String.format("Unsupported Keywords '%s'. \n%s\n%s",
                        keyword, statement.toTreeString(), nodeList);
                throw new RuntimeException(msg);

            }

        }

        /**
         *
         * @param curKeywordToken
         * @param nodes
         * @return
         */
        private static Token buildConditionNode(Token curKeywordToken, List<SqlNode> nodes) {

            List<SqlToken> expressionTokens = new ArrayList<>();

            int index = 0;
            for (; index < nodes.size(); index ++) {
                SqlNode node = nodes.get(index);
                if (node instanceof Token) {
                    expressionTokens.addAll(((Token)node).sqlTokens);
                } else {
                    throw new RuntimeException(String.format("Invalid node class type '%s'.", node.getClass()));
                }
            }

            //append ConidtionNode
            curKeywordToken.addLast(new Expression(expressionTokens));
            return curKeywordToken;
        }

        /**
         *
         * @param curKeywordToken
         * @param nodes
         * @return
         */
        private static Token buildUnionNode(Token curKeywordToken, List<SqlNode> nodes) {

            curKeywordToken.addAll(nodes);
            return curKeywordToken;
        }

        /**
         * 3种情形
         * 1. "expr"
         * 2. "expr as name"
         * TODO: 3. "expr name" 尚不支持
         *
         * @param curKeywordToken
         * @param allNodes
         * @return
         */
        private static Token buildColumnNode(Token curKeywordToken, List<SqlNode> allNodes) {

            List<List<SqlNode>> nodeList = splitByComma(allNodes);

            for (List<SqlNode> nodes: nodeList) {

                SqlNode columnNode = new SqlNode("`COLUMN`");

                //Expression Node
                List<SqlToken> expressionTokens = new ArrayList<>();

                int index = 0;
                for (; index < nodes.size(); index ++) {
                    SqlNode node = nodes.get(index);
                    if (node instanceof Token) {
                        if ("AS".equals(node.toString().toUpperCase())) {
                            break;
                        } else {

                            expressionTokens.addAll(((Token)node).sqlTokens);
                        }
                    } else {
                        throw new RuntimeException(String.format("Invalid node class type '%s'.", node.getClass()));
                    }
                }
                SqlNode exprNode = new SqlNode("`EXPR`");
                exprNode.addLast(new Expression(expressionTokens));
                columnNode.addLast(exprNode);

                //Name Node
                if (index < nodes.size()) {
                    SqlNode nameNode = new SqlNode("`NAME`");
                    nameNode.addLast(nodes.get(nodes.size()-1));
                    columnNode.addLast(nameNode);
                }

                //append ColumnNode
                curKeywordToken.addLast(columnNode);
            }
            return curKeywordToken;
        }

        /**
         * 1种情形
         * 1. with a as (select ...), b as (select ...) select ...
         *
         * @param curKeywordToken
         * @param allNodes
         * @return
         */
        private static Token buildWithNode(Token curKeywordToken, List<SqlNode> allNodes) {

            List<List<SqlNode>> nodeList = splitByComma(allNodes);

            for (List<SqlNode> nodes: nodeList) {

                assert nodes.size() == 3 || nodes.size() == 4;

                //nodes: "name AS statement"
                SqlNode withSelectNode = new SqlNode("`WITH-SELECT`");

                SqlNode nameNode = new SqlNode("`NAME`");
                nameNode.addLast(nodes.get(0));
                withSelectNode.addLast(nameNode);

                withSelectNode.addLast(nodes.get(2));

                //append withSelectNode
                curKeywordToken.addLast(withSelectNode);

                //last select clause
                if (nodes.size() == 4) {
                    curKeywordToken.addLast(nodes.get(3));
                }
            }
            return curKeywordToken;
        }

        /**
         * 3 种情形
         * 1. "name"
         * 2. "statment as name"
         * 3. "statment name"
         *
         * @param curKeywordToken
         * @param allNodes
         * @return
         */
        private static Token buildTableNode(Token curKeywordToken, List<SqlNode> allNodes) {

            List<List<SqlNode>> nodeList = splitByComma(allNodes);

            for (List<SqlNode> nodes: nodeList) {
                SqlNode tableNode = new SqlNode("`TABLE`");

                if (nodes.size() == 1) {
                    if ("`STATEMENT`".equals(nodes.get(0).toString())) {
                        tableNode.addLast(nodes.get(0));
                    } else {
                        //NameNode
                        SqlNode nameNode = new SqlNode("`NAME`");
                        nameNode.addLast(nodes.get(0));
                        tableNode.addLast(nameNode);
                    }
                } else if (nodes.size() == 2 || nodes.size() == 3) {
                    //StatementNode
                    tableNode.addLast(nodes.get(0));

                    //NameNode
                    SqlNode nameNode = new SqlNode("`NAME`");
                    nameNode.addLast(nodes.get(nodes.size()-1));
                    tableNode.addLast(nameNode);
                } else {
                    throw new RuntimeException(String.format("Invalid sql statement '%s'.", nodes.toString()));
                }

                curKeywordToken.addLast(tableNode);
            }
            return curKeywordToken;
        }

        /**
         * 2种情形
         * 1. create table $table_name as ($select_clause) [with data]
         * 2. create table $table_name as ($column_name $datatype, $column_name $datatype, ...)
         *
         * $table_name   形如 "a", "qtemp/a"
         * $datatype     形如 "varchar(32)", "int", "decimal(12,3)"。
         *  其它属性包括"NOT NULL", "PRIMARY KEY", "DEFAULT $value"，举例: "int NOT NULL PRIMARY KEY DEFAULT 1"
         *
         *
         * @param curKeywordToken
         * @param nodes
         * @return
         */
        private static Token buildCreateTableNode(Token curKeywordToken, List<SqlNode> nodes) {

            if (nodes.size() >= 3 && "AS".equalsIgnoreCase(nodes.get(nodes.size()-2).toString())) {
                //
                //case 1. create table $table_name as ($select_clause) [with data]
                //

                //NameNode
                //TODO: ["qtemp", "/", "t_xxx"] => "qtemp/t_xxx"
                SqlNode nameNode = new SqlNode("`NAME`");
                for (int i = 0; i < nodes.size()-2; i ++) {
                    nameNode.addLast(nodes.get(i));
                }
                curKeywordToken.addLast(nameNode);

                //StatementNode
                curKeywordToken.addLast(nodes.get(nodes.size()-1));

            } else {
                //
                //TODO: case 2. create table $table_name as ($column_name $datatype, $column_name $datatype, ...)
                //

                throw new UnsupportedOperationException(String.format("Unsupported Statement"));

            }

            return curKeywordToken;
        }

        /**
         * 2种情形
         * 1. insert into $table_name ($column_name, $clumn_name, ...) values ($v, $v, ...), ($v, $v, ...)
         * 2. insert into $table_name ($column_name, $clumn_name, ...) $select_clause
         *
         * $table_name   形如 "a", "qtemp/a"
         *
         *
         * @param curKeywordToken
         * @param nodes
         * @return
         */
        private static Token buildInsertIntoNode(Token curKeywordToken, List<SqlNode> nodes) {

            if (!"`STATEMENT`".equals(nodes.get(nodes.size()-1).toString())) {
                //
                //TODO: case 1. insert into $table_name ($column_name, $clumn_name, ...) values ($v, $v, ...), ($v, $v, ...)
                //

                throw new UnsupportedOperationException(String.format("Unsupported Statement"));

            } else {
                //
                //case 2. insert into $table_name ($column_name, $clumn_name, ...) $select_clause
                //

                SqlNode tableNode = new SqlNode("`TABLE`");

                SqlNode tableNameNode = new SqlNode("`NAME`");
                tableNameNode.addLast(nodes.get(0));
                tableNode.addLast(tableNameNode);

                for (int i = 2; i < nodes.size()-2; i += 2) {
                    SqlNode columnNameNode = new SqlNode(("`NAME`"));
                    columnNameNode.addLast(nodes.get(i));

                    SqlNode columnNode = new SqlNode("`COLUMN`");
                    columnNode.addLast(columnNameNode);

                    tableNode.addLast(columnNode);
                }

                curKeywordToken.addLast(tableNode);


                curKeywordToken.addLast(nodes.get(nodes.size()-1));

            }

            return curKeywordToken;
        }


        /**
         * 1种情形
         * 1. update $table_name set expr1, expr2 where expr
         *
         *
         * @param curKeywordToken
         * @param nodes
         * @return
         */
        private static Token buildUpdateNode(Token curKeywordToken, List<SqlNode> nodes) {

            SqlNode tableNode = new SqlNode("`TABLE`");

            SqlNode tableNameNode = new SqlNode("`NAME`");
            tableNameNode.addLast(nodes.get(0));
            tableNode.addLast(tableNameNode);

            curKeywordToken.addLast(tableNode);

            return curKeywordToken;
        }

        /**
         * 1种情形
         * 1. set col1=expr1, col2=expr2, ...
         *
         *
         * @param curKeywordToken
         * @param allNodes
         * @return
         */
        private static Token buildSetNode(Token curKeywordToken, List<SqlNode> allNodes) {

            List<List<SqlNode>> nodeList = splitByComma(allNodes);

            for (List<SqlNode> nodes: nodeList) {

                SqlNode columnNode = new SqlNode("`COLUMN`");

                //ColumnName
                SqlNode columnNameNode = new SqlNode("`NAME`");
                columnNameNode.addLast(nodes.get(0));
                columnNode.addLast(columnNameNode);

                //ColumnExpr
                List<SqlToken> expressionTokens = new ArrayList<>();
                for (int i = 2; i < nodes.size(); i ++) {
                    expressionTokens.addAll(((Token)nodes.get(i)).sqlTokens);
                }
                SqlNode columnExprNode = new SqlNode("`EXPR`");
                columnExprNode.addLast(new Expression(expressionTokens));
                columnNode.addLast(columnExprNode);

                curKeywordToken.addLast(columnNode);

            }
            return curKeywordToken;
        }

        /**
         * 1种情形
         * 1. delete [*] from $table_name where expr
         *
         *
         * @param curKeywordToken
         * @param nodes
         * @return
         */
        private static Token buildDeleteNode(Token curKeywordToken, List<SqlNode> nodes) {

            //do nothing
            return curKeywordToken;
        }

        /**
         * 1种情形
         * 1. group by expr, expr, ...
         *
         * @param curKeywordToken
         * @param allNodes
         * @return
         */
        private static Token buildGroupByNode(Token curKeywordToken, List<SqlNode> allNodes) {

            List<List<SqlNode>> nodeList = splitByComma(allNodes);

            for (List<SqlNode> nodes: nodeList) {

                //Expression Node
                List<SqlToken> expressionTokens = new ArrayList<>();

                int index = 0;
                for (; index < nodes.size(); index ++) {
                    SqlNode node = nodes.get(index);
                    expressionTokens.addAll(((Token)node).sqlTokens);
                }
                SqlNode exprNode = new SqlNode("`EXPR`");
                exprNode.addLast(new Expression(expressionTokens));

                //append ColumnNode
                curKeywordToken.addLast(exprNode);
            }
            return curKeywordToken;
        }

        /**
         * 1种情形
         * 1. order by expr [asc|desc], expr [asc|desc], ...
         *
         * @param curKeywordToken
         * @param allNodes
         * @return
         */
        private static Token buildOrderByNode(Token curKeywordToken, List<SqlNode> allNodes) {

            List<List<SqlNode>> nodeList = splitByComma(allNodes);

            for (List<SqlNode> nodes: nodeList) {

                //Expression Node
                List<SqlToken> expressionTokens = new ArrayList<>();

                int index = 0;
                for (; index < nodes.size(); index ++) {
                    SqlNode node = nodes.get(index);
                    expressionTokens.addAll(((Token)node).sqlTokens);
                }
                SqlNode exprNode = new SqlNode("`EXPR`");
                exprNode.addLast(new Expression(expressionTokens));

                //append ColumnNode
                curKeywordToken.addLast(exprNode);
            }
            return curKeywordToken;
        }
        /**
         * 按逗号, 分隔nodes数组
         *       `(a+1) as a, b, f(x,g(y,z)) as c`
         * ==>  [`(a+1) as a`, `b`, `f(x) as c`]
         *
         * @param allNodes
         * @return
         */
        private static List<List<SqlNode>> splitByComma(List<SqlNode> allNodes) {

            if (allNodes == null || allNodes.size() == 0) return null;

            List<List<SqlNode>> tokensList = new ArrayList<>();

            int parenCount = 0;

            List<SqlNode> tokens = new ArrayList<>();
            for (SqlNode n: allNodes) {

                if (TOKEN_LEFT_PAREN.equals(n.toString())) {
                    parenCount ++;
                    tokens.add(n);
                } else if (TOKEN_RIGHT_PAREN.equals(n.toString())) {
                    parenCount --;
                    tokens.add(n);
                } else if (TOKEN_COMMA.equals(n.toString())) {
                    if (parenCount == 0) {
                        tokensList.add(tokens);
                        tokens = new ArrayList<>();
                    } else {
                        tokens.add(n);
                    }
                } else {
                    tokens.add(n);
                }
            }

            //last one
            tokensList.add(tokens);

            return tokensList;
        }

        /**
         *
         * @param sqlTokens
         * @return
         */
        public static Node buildSimpleAST(List<SqlToken> sqlTokens) {

            //更新Keywords
            List<Token> tokens = new ArrayList<>();
            for (int i = 0; i < sqlTokens.size(); i ++) {
                SqlToken currToken = sqlTokens.get(i);

                boolean bKeyword = KEYWORDS.contains(currToken.getText().toUpperCase());

                tokens.add(new Token(currToken, bKeyword));
            }

            //GO
            Statement statement = buildNestedStatement(tokens);
            statement = buildNodes(statement);
            return statement;

        }
    }
}
