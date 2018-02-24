package me.tony9.sql;

import me.tony9.util.tree.Node;
import me.tony9.util.tree.NodeList;

import java.util.*;
import java.util.stream.Collectors;

public class SqlParser {

    private static Set<String> KEYWORDS;
    static {
        String[] statements = new String[] {
                //Table
                "CREATE TABLE WITH DATA",
                "SELECT FROM [LEFT|RIGHT|INNER|OUTER] JOIN ON WHERE GROUP BY HAVING ORDER BY LIMIT UNION [ALL]",
                "WITH SELECT FROM SELECT FROM SELECT FROM",
                "INSERT INTO VALUES",
                "DELETE FROM",
                "DROP TABLE",
                //Index
                "CREATE INDEX ON",
                "DROP INDEX",
                //Expression
//                "AND", "OR", "NOT"
        };

        KEYWORDS = new HashSet<>();
        for (int i = 0; i < statements.length; i ++) {
            for (String w : statements[i].split("[\\s\\[\\]\\|]+")) {
                KEYWORDS.add(w);
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

    }

    public Node parse(String sql) {
        SqlTokenizer sqlTokenizer = new SqlTokenizer();
        List<SqlToken> tokens = sqlTokenizer.split(sql);
        return SqlASTBuilder.buildSimpleAST(tokens);
    }


    private static class SqlASTBuilder {

        private static String LEFT_PAREN = "(";
        private static String RIGHT_PAREN = ")";
        private static String COMMA = ",";

        private static String TOKEN_SELECT = "SELECT";


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

        private static List<Interval<Integer>> findAllParenPairs(List<SqlToken> sqlTokens) {

            List<Interval<Integer>> intervals = new ArrayList<>();

            LinkedList<Integer> stack = new LinkedList<>();

            for (int i = 0; i < sqlTokens.size(); i++) {

                SqlToken sqlToken = sqlTokens.get(i);
                if (sqlToken.getText().equals(LEFT_PAREN)) {

                    stack.push(i);

                } else if (sqlToken.getText().equals(RIGHT_PAREN)) {

                    Integer start = stack.pop();
                    Integer stop = i;
                    intervals.add(new Interval<>(start, stop));
                }
            }

            return intervals;
        }


        /**
         * 分析嵌套子句，组装Statement对象
         * select a,b
         * from (
         *      select a,b
         *      from (
         *          select a,b
         *          from t1
         *      ) as t2
         * ) as t3
         * ;
         *
         * create table t (
         *      select a,b
         *      from t
         * ) with test
         * ;
         *
         * @param sqlTokens
         * @return
         */
        private static Statement buildNestedStatement(List<SqlToken> sqlTokens) {

            Set<Integer> nestedStatementStopIndex = findAllParenPairs(sqlTokens).stream()
                    .filter(interval -> TOKEN_SELECT.equals(sqlTokens.get(interval.start + 1).getText().toUpperCase()))
                    .map(interval -> interval.stop)
                    .collect(Collectors.toSet());

            //statement
            LinkedList<Node> stack = new LinkedList<>();

            for (int i = 0; i < sqlTokens.size(); i++) {

                if (nestedStatementStopIndex.contains(i)) {

                    // 遍历到 "（ SELECT ... )" 的结束符号")"，创建对应的nestedStatement
                    // 1. 找到对应的 "("
                    // 2. 将"(", ")"之间的Node作为nestedStatement的子节点
                    Statement nestedStatement = new Statement();

                    int parenCount = 0;
                    Node n = null;
                    while (true) {
                        n = stack.pop();
                        if (n instanceof Token) {
                            Token t = (Token)n;

                            if (LEFT_PAREN.equals(t.toString())) {
                                parenCount ++;
                            } else if (RIGHT_PAREN.equals(t.toString())) {
                                parenCount --;
                            }

                            if (parenCount == 1) {  //跳出while循环
                                break;
                            } else {
                                nestedStatement.addFirst(n);
                            }

                        } else {
                            nestedStatement.addFirst(n);
                        }
                    }

                    stack.push(nestedStatement);

                } else {
                    SqlToken currToken = sqlTokens.get(i);

                    boolean bKeyword = KEYWORDS.contains(currToken.getText().toUpperCase());

                    stack.push(new Token(currToken, bKeyword));
                }
            }

            //return
            Statement s = new Statement();
            while (!stack.isEmpty()) {
                Node t = stack.pop();
                s.addFirst(t);
            }
            return s;
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

            Statement newStatement = new Statement();

            LinkedList<SqlNode> nodes = new LinkedList<>();
            Token curKeywordToken = null;

            for (int i = 0; i < nestedStatement.getChildren().size(); i ++) {

                SqlNode node = (SqlNode)nestedStatement.getChildren().get(i);

                if (node instanceof Token && ((Token) node).keyword) {
                    if (curKeywordToken != null) {

                        if (nodes.size() > 0) {
                            List<List<SqlNode>> nodeList = splitByComma(nodes);
                            buildNode(newStatement, curKeywordToken, nodeList);

                            //reset nodes and curKeywordToken
                            nodes = new LinkedList<>();
                            curKeywordToken = new Token((Token) node);

                        } else {    //合并连续的keywords
                            curKeywordToken.append((Token) node);
                        }

                    } else {
                        curKeywordToken = new Token((Token) node);
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

                List<List<SqlNode>> nodeList = splitByComma(nodes);
                buildNode(newStatement, curKeywordToken, nodeList);
            }

            return newStatement;
        }

        private static void buildNode(Statement statement, Token curKeywordToken, List<List<SqlNode>> nodeList) {

            Token curToken = null;
            String keyword = curKeywordToken.toString();
            if ("`SELECT`".equals(keyword)) {

                curToken = buildColumnNode(curKeywordToken, nodeList);
                statement.addLast(curToken);

            } else if ("`WITH`".equals(keyword)) {
                curToken = buildWithNode(curKeywordToken, nodeList);
                statement.addLast(curToken);
            } else if ("`FROM`".equals(keyword) || keyword.endsWith("JOIN`")) {
                curToken = buildTableNode(curKeywordToken, nodeList);
                statement.addLast(curToken);
            } else if ("`ON`".equals(keyword) || "`WHERE`".equals(keyword)) {
                curToken = buildConidtionNode(curKeywordToken, nodeList);
                if ("`ON`".equals(keyword)) {
                    NodeList<SqlNode> nodes = statement.getChildren();
                    nodes.get(nodes.size()-1).addLast(curToken);
                } else {
                    statement.addLast(curToken);
                }

            } else if ("`GROUP-BY`".equals(keyword)) {
                curToken = buildGroupByNode(curKeywordToken, nodeList);
                statement.addLast(curToken);
            } else if ("`ORDER-BY`".equals(keyword)) {
                curToken = buildOrderByNode(curKeywordToken, nodeList);
                statement.addLast(curToken);
            } else if ("`CREATE-TABLE`".equals(keyword)) {
                curToken = buildCreateTableNode(curKeywordToken, nodeList);
                statement.addLast(curToken);
            } else if ("`WITH-DATA`".equals(keyword)) {
                statement.addLast(curKeywordToken);
            } else {
                throw new RuntimeException(String.format("Unsupported Keywords '%s'.", keyword));
            }

        }

        private static Token buildConidtionNode(Token curKeywordToken, List<List<SqlNode>> nodeList) {

            assert nodeList.size() == 1;

            List<SqlNode> nodes = nodeList.get(0);
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

        private static Token buildColumnNode(Token curKeywordToken, List<List<SqlNode>> nodeList) {

            for (List<SqlNode> nodes: nodeList) {

                //三种情形
                //1. "expr"
                //2. "expr as name"
                //TODO: 3. "expr name"
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

        private static Token buildWithNode(Token curKeywordToken, List<List<SqlNode>> nodeList) {

            for (List<SqlNode> nodes: nodeList) {

                assert nodes.size() == 3;

                //nodes: "name AS statement"
                SqlNode withSelectNode = new SqlNode("`WITH-SELECT`");

                SqlNode nameNode = new SqlNode("`NAME`");
                nameNode.addLast(nodes.get(0));
                withSelectNode.addLast(nameNode);

                withSelectNode.addLast(nodes.get(2));
                //append ColumnNode
                curKeywordToken.addLast(withSelectNode);
            }
            return curKeywordToken;
        }

        private static Token buildTableNode(Token curKeywordToken, List<List<SqlNode>> nodeList) {

            //三种情形
            //1. "name"
            //2. "statment as name"
            //3. "statment name"
            for (List<SqlNode> nodes: nodeList) {
                SqlNode tableNode = new SqlNode("`TABLE`");

                if (nodes.size() == 1) {
                    //NameNode
                    SqlNode nameNode = new SqlNode("`NAME`");
                    nameNode.addLast(nodes.get(0));
                    tableNode.addLast(nameNode);
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
         * create table name as (column_a, column_b, ...);
         * create table name as (statement);
         * create table name as (statement) with data;
         *
         * @param curKeywordToken
         * @param nodeList
         * @return
         */
        private static Token buildCreateTableNode(Token curKeywordToken, List<List<SqlNode>> nodeList) {

            assert nodeList.size() == 1;

            List<SqlNode> nodes = nodeList.get(0);

            assert nodes.size() == 3;

            //NameNode
            SqlNode nameNode = new SqlNode("`NAME`");
            nameNode.addLast(nodes.get(0));
            curKeywordToken.addLast(nameNode);

            //StatementNode
            curKeywordToken.addLast(nodes.get(nodes.size()-1));

            return curKeywordToken;
        }

        private static Token buildGroupByNode(Token curKeywordToken, List<List<SqlNode>> nodeList) {

            for (List<SqlNode> nodes: nodeList) {

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

                //append ColumnNode
                curKeywordToken.addLast(exprNode);
            }
            return curKeywordToken;
        }

        private static Token buildOrderByNode(Token curKeywordToken, List<List<SqlNode>> nodeList) {

            for (List<SqlNode> nodes: nodeList) {

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

                //append ColumnNode
                curKeywordToken.addLast(exprNode);
            }
            return curKeywordToken;
        }
        /**
         * 按COMMA, 分隔nodes数组
         *       `(a+1) as a, b, f(x,g(y,z)) as c`
         * ==>  [`(a+1) as a`, `b`, `f(x) as c`]
         *
         * @param nodes
         * @return
         */
        private static List<List<SqlNode>> splitByComma(List<SqlNode> nodes) {

            if (nodes == null || nodes.size() == 0) return null;

            List<List<SqlNode>> tokensList = new ArrayList<>();

            int parenCount = 0;

            List<SqlNode> tokens = new ArrayList<>();
            for (SqlNode n: nodes) {

                if (LEFT_PAREN.equals(n.toString())) {
                    parenCount ++;
                    tokens.add(n);
                } else if (RIGHT_PAREN.equals(n.toString())) {
                    parenCount --;
                    tokens.add(n);
                } else if (COMMA.equals(n.toString())) {
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

            Statement statement = buildNestedStatement(sqlTokens);
            statement = buildNodes(statement);

            //fix with statement
            if ("`WITH`".equals(statement.getChildren().get(0).toString())) {
                NodeList<SqlNode> nodes = statement.getChildren();

                Statement withStatement = new Statement();
                withStatement.addLast(nodes.get(0));

                Statement lastSelectStatement = new Statement();
                for (int i = 1; i < nodes.size(); i ++) {
                    lastSelectStatement.addLast(nodes.get(i));
                }
                withStatement.addLast(lastSelectStatement);

                return withStatement;

            } else {
                return statement;
            }

        }
    }
}
