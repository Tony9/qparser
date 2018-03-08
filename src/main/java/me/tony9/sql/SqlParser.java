package me.tony9.sql;

import me.tony9.util.tree.Node;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.swing.plaf.nimbus.State;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class SqlParser {

    private static Log logger = LogFactory.getLog(SqlParser.class);

    private final static Set<String> KEYWORDS;
    static {
        String[] statements = new String[] {
                //DDL
                "CREATE TABLE",
                // "IF NOT EXISTS",
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
                "OVER PARTITION BY",
                //逻辑表达式
//                "AND OR NOT"

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
                "CREATE-TABLE IF-NOT-EXISTS WITH-DATA",
                "CREATE-INDEX",
                "DROP-TABLE IF-EXISTS",
                "DROP-INDEX",
                //DML
                "SELECT-STREAM",
                "LEFT-JOIN RIGHT-JOIN INNER-JOIN OUTER-JOIN",
                "GROUP-BY ORDER-BY PARTITION-BY UNION-ALL",
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
            this.sqlTokens = new ArrayList<>();
            this.sqlTokens.addAll(t.sqlTokens);
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

    private static class TokenUtils {

        private static String TOKEN_LEFT_PAREN = "(";
        private static String TOKEN_RIGHT_PAREN = ")";


        private static List<Interval<Integer>> findAllParenPairs(List<Token> tokens, String strStart, String strStop) {

            List<Interval<Integer>> intervals = new ArrayList<>();

            LinkedList<Integer> stack = new LinkedList<>();

            for (int i = 0; i < tokens.size(); i++) {

                Token token = tokens.get(i);
                if (token.toString().equals(strStart)) {

                    stack.push(i);

                } else if (token.toString().equals(strStop)) {

                    Integer start = stack.pop();
                    Integer stop = i;
                    intervals.add(new Interval<>(start, stop));
                }
            }

            return intervals;
        }

        private static int findPosition(LinkedList<Node<SqlNode>> nodes, int startIndex, int stopIndex, Predicate<Node<SqlNode>> predicate) {

            List<Integer> posList = findPositions(nodes, startIndex, stopIndex, predicate);
            return posList.isEmpty()?-1:posList.get(0);
        }

        private static List<Integer> findPositions(LinkedList<Node<SqlNode>> nodes, int startIndex, int stopIndex, Predicate<Node<SqlNode>> predicate) {

            startIndex = (startIndex>0)?startIndex:0;
            stopIndex = (stopIndex<nodes.size())?stopIndex:nodes.size();

            List<Integer> posList = new ArrayList<>();

            int parenCount = 0;

            for (int i = startIndex; i < stopIndex; i ++) {
                Node<SqlNode> n = nodes.get(i);
                if (TOKEN_LEFT_PAREN.equals(n.toString())) {
                    parenCount ++;
                } else if (TOKEN_RIGHT_PAREN.equals(n.toString())) {
                    parenCount--;
                }
                if (parenCount == 0 && predicate.test(n)) {
                    posList.add(i);
                }
            }
            return posList;
        }

        private static int findPosition(LinkedList<Node<SqlNode>> nodes, String keyword) {

            return findPosition(nodes, 0, nodes.size(), n -> keyword.equals(n.toString().toUpperCase()));
        }

        private static List<Integer> findPositions(LinkedList<Node<SqlNode>> nodes, Predicate<Node<SqlNode>> predicate) {
            return findPositions(nodes, 0, nodes.size(), predicate);
        }
    }

    private static class SqlASTBuilder {

        private static String TOKEN_LEFT_PAREN = "(";
        private static String TOKEN_RIGHT_PAREN = ")";
        private static String TOKEN_COMMA = ",";
        private static String TOKEN_UNION = "`UNION`";
        private static String TOKEN_UNION_ALL = "`UNION-ALL`";

        private static String TOKEN_SELECT = "`SELECT`";

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
                    if (TOKEN_UNION.equals(s) || TOKEN_UNION_ALL.equals(s)) {
                        unionNodePos.add(i);
                        newTokens.add(t);
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

        /**
         * 分析Union语句
         *
         * @param tokens
         * @param unionNodePos
         * @return
         */
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

            statement = rebuildLastSelectStatement(statement);

            //TODO: 处理最后一句的OrderBy、limit

            return statement;
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
            List<Interval<Integer>> allSubQueryPairs = TokenUtils.findAllParenPairs(tokens, TOKEN_LEFT_PAREN, TOKEN_RIGHT_PAREN).stream()
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
         * 处理 Union 语句
         * @param unionStatement
         * @return
         */
        private static Statement buildUnionQueryStatementNode(UnionStatement unionStatement) {

            LinkedList<Node<SqlNode>> children = unionStatement.getChildren();

            //stack: A1 u1 A2 u2 A3 u3 A4
            //每次出栈三个元素 X u Y，将结果 X u Y 放回栈
            LinkedList<Node<SqlNode>> stack = new LinkedList<>();
            for (Node<SqlNode> n: children) {
                stack.add(n);
            }
            while (stack.size() >= 3) {
                Node s1 = stack.pop();
                if (s1 instanceof Statement) s1 = buildQueryStatementNode((Statement)s1);

                Node n = stack.pop();

                Statement s2 = (Statement)stack.pop();
                if (s2 instanceof Statement) s2 = buildQueryStatementNode((Statement)s2);

                n.addLast(s1);
                n.addLast(s2);

                stack.push(n);
            }

            Statement newStatement = new Statement();

            newStatement.addLast(stack.pop());


            return newStatement;
        }

        /**
         * 处理 Simple 语句 (非Union、单个语句）。包括：
         * - Create
         * - Drop
         * - Delete
         * - Update
         * - Insert
         * - Select/With..Select
         * ...
         *
         * @param nestedStatement
         * @return
         */
        private static Statement buildNodes(Statement nestedStatement) {

            if (nestedStatement instanceof UnionStatement) {

                return buildUnionQueryStatementNode((UnionStatement) nestedStatement);

            } else {
                String node0 = nestedStatement.getChildren().get(0).toString();

                if ("`CREATE-TABLE`".equals(node0)) {
                    return buildCreateStatementNode(nestedStatement);
                } else if ("`INSERT-INTO`".equals(node0)) {
                    return buildInsertStatementNode(nestedStatement);
                } else if ("`DELETE`".equals(node0)) {
                    return buildDeleteStatementNode(nestedStatement);
                } else if ("`UPDATE`".equals(node0)) {
                    return buildUpdateStatementNode(nestedStatement);
                } else if ("`SELECT`".equals(node0) || "`WITH`".equals(node0)) {
                    return buildQueryStatementNode(nestedStatement);
                } else {
                    throw new RuntimeException(String.format("Wrong Statement: %s", node0));
                }
            }


        }

        private static List<SqlNode> subList(LinkedList<Node<SqlNode>> nodes, int startIndex, int stopIndex) {
            List<SqlNode> sub = new ArrayList<>();
            for (int i = startIndex; i < stopIndex; i ++) {
                sub.add((SqlNode) nodes.get(i));
            }
            return sub;
        }


        private static Statement buildQueryStatementNode(Statement queryStatement) {

            if (queryStatement instanceof UnionStatement) {
                return buildUnionQueryStatementNode((UnionStatement) queryStatement);
            } else {

                String node0 = queryStatement.getChildren().get(0).toString();
                if ("`WITH`".equals(node0)) {
                    return buildWithQueryStatementNode(queryStatement);
                } else {
                    return buildSimpleQueryStatementNode(queryStatement);
                }
            }
        }

        /**
         * 1. `WITH` [
         *      $queryName `AS` (
         *          $selectClause
         *      )
         *     ]+
         *     $lastSelectClause
         *
         * @param queryStatement
         * @return
         */
        private static Statement buildWithQueryStatementNode(Statement queryStatement) {

            Statement newStatement = new Statement();

            LinkedList<Node<SqlNode>> nodes = queryStatement.getChildren();

            //`WITH`
            SqlNode withNode = (SqlNode) nodes.get(0);
            newStatement.addLast(withNode);

            //$queryName `AS` ($selectClause)
            List<List<SqlNode>> subQuerys = splitByComma(subList(nodes, 1, nodes.size()-1));
            for (List<SqlNode> subQuery: subQuerys) {
                SqlNode subQueryNameNode = new SqlNode("`NAME`");
                subQueryNameNode.addLast(subQuery.get(0));

                SqlNode subQueryNode = new SqlNode("`SUB-QUERY`");
                subQueryNode.addLast(subQueryNameNode);
                subQueryNode.addLast(buildQueryStatementNode((Statement) subQuery.get(2)));

                withNode.addLast(subQueryNode);
            }

            //$lastSelectClause
            SqlNode lastQueryNode = new SqlNode("`LAST-QUERY`");
            lastQueryNode.addLast(buildQueryStatementNode((Statement) nodes.getLast()));
            withNode.addLast(lastQueryNode);

            //return
            return newStatement;
        }

        /**
         * 1.   `SELECT` $columns
         *      `FROM` $tables
         *      [`*JOIN` $table `ON` $joinConditions]*
         *      [`WHERE` $whereConditions]{0,1}
         *      [`GROUP-BY` $groupExprs]{0,1}
         *      [`HAVING` $havingConditions]{0,1}
         *      [`ORDER-BY` $orderExprs]{0,1}
         *      [`LIMIT` $start,$limit]{0,1}
         *      [`FOR-UPDATE` | `LOCK-IN-SHARE-MODE`]
         *
         * 2. `WITH` [$queryName `AS` ($selectClause)]+ $selectClause
         *
         * @param queryStatement
         * @return
         */
        private static Statement buildSimpleQueryStatementNode(Statement queryStatement) {

            Statement newStatement = new Statement();

            LinkedList<Node<SqlNode>> nodes = queryStatement.getChildren();

            int posSelectStart = 0;
            int posFromStart = TokenUtils.findPosition(nodes, "`FROM`");
            List<Integer> posJoinStartList = TokenUtils.findPositions(nodes, n -> n.toString().toUpperCase().endsWith("JOIN`"));
            int posWhereStart = TokenUtils.findPosition(nodes, "`WHERE`");
            int posGroupByStart = TokenUtils.findPosition(nodes, "`GROUP-BY`");
            int posHavingStart = TokenUtils.findPosition(nodes, "`HAVING`");
            int posOrderByStart = TokenUtils.findPosition(nodes, "`ORDER-BY`");
            int posLimitStart = TokenUtils.findPosition(nodes, "`LIMIT`");

            List<Integer> posKeywords = new ArrayList<>();
            posKeywords.add(posSelectStart);
            posKeywords.add(posFromStart);
            posKeywords.addAll(posJoinStartList);
            posKeywords.add(posWhereStart);
            posKeywords.add(posGroupByStart);
            posKeywords.add(posHavingStart);
            posKeywords.add(posOrderByStart);
            posKeywords.add(posLimitStart);
            posKeywords = posKeywords.stream().filter(n -> n > -1).collect(Collectors.toList());
            posKeywords.add(nodes.size());

            int posSelectStop = posKeywords.stream().filter(n -> n > posSelectStart).min(Integer::compareTo).get();
            int posFromStop = posKeywords.stream().filter(n -> n > posFromStart).min(Integer::compareTo).get();
            List<Integer> posJoinStopList = new ArrayList<>();
            for (Integer posJoinStart: posJoinStartList) {
                posJoinStopList.add(posKeywords.stream().filter(n -> n > posJoinStart).min(Integer::compareTo).get());
            }
            int posWhereStop = posKeywords.stream().filter(n -> n > posWhereStart).min(Integer::compareTo).get();
            int posGroupByStop = posKeywords.stream().filter(n -> n > posGroupByStart).min(Integer::compareTo).get();
            int posHavingStop = posKeywords.stream().filter(n -> n > posHavingStart).min(Integer::compareTo).get();
            int posOrderByStop = posKeywords.stream().filter(n -> n > posOrderByStart).min(Integer::compareTo).get();
            int posLimitStop = posKeywords.stream().filter(n -> n > posLimitStart).min(Integer::compareTo).get();

            //`SELECT` $columns
            SqlNode selectNode = (SqlNode) nodes.get(0);

            List<SqlNode> columnNodes = buildColumnNodes(subList(nodes, posSelectStart+1, posSelectStop));
            selectNode.addAll(columnNodes);

            newStatement.addLast(selectNode);

            //`FROM` $tables
            SqlNode fromNode = (SqlNode) nodes.get(posFromStart);

            List<SqlNode> tableNodes = buildTableNodes(subList(nodes, posFromStart+1, posFromStop));
            fromNode.addAll(tableNodes);

            newStatement.addLast(fromNode);

            //`*JOIN` $table `ON` $joinCondition
            for (int i = 0; i < posJoinStartList.size(); i ++) {
                int posJoinStart = posJoinStartList.get(i);
                int posJoinStop = posJoinStopList.get(i);
                int posOnStart = TokenUtils.findPosition(nodes, posJoinStart, posJoinStop, n -> "`ON`".equals(n.toString().toUpperCase()));
                int posOnStop = posJoinStop;
                posJoinStop = posOnStart;

                SqlNode joinNode = (SqlNode) nodes.get(posJoinStart);
                joinNode.addAll(buildTableNodes(subList(nodes, posJoinStart+1, posJoinStop)));

                SqlNode joinOnNode = (SqlNode) nodes.get(posOnStart);
                joinOnNode.addLast(buildExpressionNode(nodes, posOnStart+1, posOnStop));
                joinNode.addLast(joinOnNode);

                newStatement.addLast(joinNode);
            }


            //`WHERE` $whereCondition
            if (posWhereStart > -1) {
                SqlNode whereNode = (SqlNode) nodes.get(posWhereStart);
                whereNode.addLast(buildExpressionNode(nodes, posWhereStart+1, posWhereStop));

                newStatement.addLast(whereNode);
            }

            //`GROUP-BY` $groupExprs
            if (posGroupByStart > -1) {
                SqlNode groupByNode = (SqlNode) nodes.get(posGroupByStart);
                groupByNode.addAll(buildGroupByNodes(subList(nodes, posGroupByStart+1, posGroupByStop)));

                newStatement.addLast(groupByNode);
            }

            //`HAVING` $havingConditions
            if (posHavingStart > -1) {
                SqlNode havingNode = (SqlNode) nodes.get(posHavingStart);
                havingNode.addLast(buildExpressionNode(nodes, posHavingStart+1, posHavingStop));

                newStatement.addLast(havingNode);
            }

            //`ORDER-BY` $orderExprs
            if (posOrderByStart > -1) {
                SqlNode orderByNode = (SqlNode) nodes.get(posOrderByStart);
                orderByNode.addAll(buildOrderExprNodes(subList(nodes, posOrderByStart+1, posOrderByStop)));

                newStatement.addLast(orderByNode);
            }

            //TODO: `LIMIT`

            //return
            return newStatement;

        }

        /**
         * 1. `CREATE-TABLE` $tableName `AS` ($selectClause) [`WITH-DATA`]
         * TODO: 2. `CREATE-TABLE` $tableName ($columnName $dataType, $columnName $dataType, ...)
         *
         * $tableName   形如 "a", "qtemp/t_x"
         * $datatype     形如 "varchar(32)", "int", "decimal(12,3)"。
         *  其它属性包括"NOT NULL", "PRIMARY KEY", "DEFAULT $value"，举例: "int NOT NULL PRIMARY KEY DEFAULT 1"
         *
         * @param createStatement
         * @return
         */
        private static Statement buildCreateStatementNode(Statement createStatement) {

            Statement newStatement = new Statement();

            LinkedList<Node<SqlNode>> nodes = createStatement.getChildren();

            int posAS = TokenUtils.findPosition(nodes, "AS");
            if (posAS > -1) {

                //`CREATE-TABLE`
                SqlNode createTableNode = (SqlNode) nodes.get(0);

                //$tableName
                SqlNode tableNode = new SqlNode("`TABLE`");
                SqlNode tableNameNode = buildTableNameNode(nodes, 1, posAS);
                tableNode.addLast(tableNameNode);

                createTableNode.addLast(tableNode);

                //$selectClause
                Statement queryStatement = (Statement) nodes.get(posAS+1);
                createTableNode.addLast(buildQueryStatementNode(queryStatement));

                //Token after $selectClause
                for (int i = posAS+2; i < nodes.size(); i ++) {
                    createTableNode.addLast(nodes.get(i));
                }

                newStatement.addLast(createTableNode);

            } else {
                //
                // other cases
                //
                throw new RuntimeException(String.format("Unsupported SQL"));
            }

            return newStatement;
        }

        private static SqlNode buildTableNameNode(LinkedList<Node<SqlNode>> nodes, int posStart, int posStop) {

            SqlNode tableNameNode = new SqlNode("`NAME`");
            for (int i = posStart; i < posStop; i ++) {
                tableNameNode.addLast(nodes.get(i));
            }

            return tableNameNode;
        }

        private static <T> Expression buildExpressionNode(List<T> nodes, int posStart, int posStop) {

            List<SqlToken> expressionTokens = new ArrayList<>();
            for (int i = posStart; i < posStop; i ++) {
                SqlNode node = (SqlNode)nodes.get(i);
                if (node instanceof Token) {
                    expressionTokens.addAll(((Token)node).sqlTokens);
                } else {
                    throw new RuntimeException(String.format("Invalid node class type '%s'.", node.getClass()));
                }
            }

            return new Expression(expressionTokens);
        }

        /**
         * 2种情形
         * TODO: 1. `INSERT-INTO` $tableName [($columnName, $clumnName, ...)] `VALUES` ($v, $v, ...), ($v, $v, ...)
         * 2. `INSERT-INTO` $tableName [($columnName, $columnName, ...)] $selectClause
         *
         * $tableName   形如 "a", "qtemp/a"
         *
         * @param insertStatement
         * @return
         */
        private static Statement buildInsertStatementNode(Statement insertStatement) {

            Statement newStatement = new Statement();

            LinkedList<Node<SqlNode>> nodes = insertStatement.getChildren();

            if ("`STATEMENT`".equals(nodes.getLast().toString())) {
                //
                // case 2. insert...select...
                //

                int posTableNameStart, posTableNameStop;
                int posColumnNameStart, posColumnNameStop;
                if (TOKEN_RIGHT_PAREN.equals(nodes.get(nodes.size()-2).toString())) {
                    posColumnNameStart = -1;
                    for (int i = 0; i < nodes.size(); i ++) {
                        if (TOKEN_LEFT_PAREN.equals(nodes.get(i).toString())) {
                            posColumnNameStart = i;
                            break;
                        }
                    }
                    posColumnNameStop = nodes.size()-2;

                    posTableNameStop = posColumnNameStart;
                    posTableNameStart = 1;
                } else {
                    posColumnNameStart = -1;
                    posColumnNameStop = -1;

                    posTableNameStart = 1;
                    posTableNameStop = nodes.size()-1;
                }

                //`INSERT-INTO`
                SqlNode insertIntoNode = (SqlNode)nodes.get(0);

                SqlNode tableNode = new SqlNode("`TABLE`");
                //$tableName
                SqlNode tableNameNode = buildTableNameNode(nodes, posTableNameStart, posTableNameStop);
                tableNode.addLast(tableNameNode);
                //$columnName
                if (posColumnNameStart > -1 && posColumnNameStop > posColumnNameStart) {
                    for (int i = posColumnNameStart+1; i < posColumnNameStop; i += 2) {
                        SqlNode columnNameNode = new SqlNode("`NAME`");
                        columnNameNode.addLast(nodes.get(i));
                        SqlNode columnNode = new SqlNode("`COLUMN`");
                        columnNode.addLast(columnNameNode);

                        tableNode.addLast(columnNode);
                    }
                }

                insertIntoNode.addLast(tableNode);

                //$selectClause
                Statement statement = (Statement)nodes.getLast();
                insertIntoNode.addLast(buildQueryStatementNode(statement));

                //over
                newStatement.addLast(insertIntoNode);

            } else {

                //
                // case 1. insert...values...
                //
                throw new RuntimeException(String.format("Unsupported SQL"));

            }

            return newStatement;
        }

        /**
         * 1. `DELETE` [*] `FROM` $tableName `WHERE` $boolExpr
         *
         * $tableName   形如 "a", "qtemp/a"
         *
         * @param insertStatement
         * @return
         */
        private static Statement buildDeleteStatementNode(Statement insertStatement) {

            Statement newStatement = new Statement();

            LinkedList<Node<SqlNode>> nodes = insertStatement.getChildren();

            //`DELETE`
            SqlNode deleteNode = (SqlNode)nodes.get(0);
            newStatement.addLast(deleteNode);


            int posFromStart = TokenUtils.findPosition(nodes, "`FROM`");
            int posWhereStart = TokenUtils.findPosition(nodes, "`WHERE`");
            int posFromStop = (posWhereStart == -1)?nodes.size():posWhereStart;
            int posWhereEnd = nodes.size();

            //`FROM-TABLE-NAME`
            SqlNode tableNameNode = buildTableNameNode(nodes, posFromStart+1, posFromStop);

            SqlNode tableNode = new SqlNode("`TABLE`");
            tableNode.addLast(tableNameNode);

            SqlNode fromNode = new SqlNode("`FROM`");
            fromNode.addLast(tableNode);

            newStatement.addLast(fromNode);

            //`WHERE`
            if (posWhereStart > -1) {
                SqlNode whereNode = new SqlNode("`WHERE`");
                whereNode.addLast(buildExpressionNode(nodes, posWhereStart+1, posWhereEnd));

                newStatement.addLast(whereNode);
            }

            return newStatement;
        }


        /**
         * 1. `UPDATE` $tableName `SET` $col1=$expr1, $col2=$expr2 `WHERE` $boolExpr
         *
         * $tableName   形如 "a", "qtemp/a"
         *
         * @param updateStatement
         * @return
         */
        private static Statement buildUpdateStatementNode(Statement updateStatement) {

            Statement newStatement = new Statement();

            LinkedList<Node<SqlNode>> nodes = updateStatement.getChildren();

            //`UPDATE`
            SqlNode updateNode = (SqlNode)nodes.get(0);
            newStatement.addLast(updateNode);


            int posSetStart = TokenUtils.findPosition(nodes, "`SET`");
            int posWhereStart = TokenUtils.findPosition(nodes, "`WHERE`");
            int posSetStop = (posWhereStart == -1)?nodes.size():posWhereStart;
            int posWhereEnd = nodes.size();

            //$tableName
            SqlNode tableNameNode = buildTableNameNode(nodes, 1, posSetStart);

            SqlNode tableNode = new SqlNode("`TABLE`");
            tableNode.addLast(tableNameNode);

            updateNode.addLast(tableNode);

            //`SET`
            SqlNode setNode = new SqlNode("`SET`");
            List<SqlNode> subNodes = new ArrayList<SqlNode>();
            for (int i = posSetStart+1; i < posSetStop; i ++) {
                subNodes.add((SqlNode)nodes.get(i));
            }
            List<List<SqlNode>> assignments = splitByComma(subNodes);
            for (int i = 0; i < assignments.size(); i ++) {

                List<SqlNode> assignment = assignments.get(i);

                SqlNode columnNode = new SqlNode("`COLUMN`");

                SqlNode nameNode = new SqlNode("`NAME`");
                nameNode.addLast(assignment.get(0));
                columnNode.addLast(nameNode);

                SqlNode exprNode = new SqlNode("`EXPR`");
                exprNode.addLast(buildExpressionNode(assignment, 2, assignment.size()));
                columnNode.addLast(exprNode);

                setNode.addLast(columnNode);
            }
            newStatement.addLast(setNode);

            //`WHERE`
            if (posWhereStart > -1) {
                SqlNode whereNode = new SqlNode("`WHERE`");
                whereNode.addLast(buildExpressionNode(nodes, posWhereStart+1, posWhereEnd));

                newStatement.addLast(whereNode);
            }

            return newStatement;
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
         * @param allNodes
         * @return
         */
        private static List<SqlNode> buildColumnNodes(List<SqlNode> allNodes) {

            List<SqlNode> columnNodes = new ArrayList<>();

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
                columnNodes.add(columnNode);
            }
            return columnNodes;
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
         * @param allNodes
         * @return
         */
        private static List<SqlNode> buildTableNodes(List<SqlNode> allNodes) {

            List<SqlNode> tableNodes = new ArrayList<>();

            List<List<SqlNode>> nodeList = splitByComma(allNodes);

            for (List<SqlNode> nodes: nodeList) {

                SqlNode tableNode = new SqlNode("`TABLE`");

                SqlNode node0 = nodes.get(0);

                if (nodes.size() == 1) {

                    if (node0 instanceof Statement) {
                        Statement statement = (Statement) node0;
                        tableNode.addLast(buildQueryStatementNode(statement));
                    } else {
                        //NameNode
                        SqlNode nameNode = new SqlNode("`NAME`");
                        nameNode.addLast(nodes.get(0));
                        tableNode.addLast(nameNode);
                    }

                } else if (nodes.size() == 2 || nodes.size() == 3) {
                    if (node0 instanceof Statement) {
                        Statement statement = (Statement) node0;
                        tableNode.addLast(buildQueryStatementNode(statement));
                    } else {
                        //NameNode
                        SqlNode nameNode = new SqlNode("`EXPR`");
                        nameNode.addLast(nodes.get(0));
                        tableNode.addLast(nameNode);
                    }

                    //NameNode
                    SqlNode nameNode = new SqlNode("`NAME`");
                    nameNode.addLast(nodes.get(nodes.size()-1));
                    tableNode.addLast(nameNode);
                } else {
                    throw new RuntimeException(String.format("Invalid sql statement '%s'.", nodes.toString()));
                }

                tableNodes.add(tableNode);
            }
            return tableNodes;
        }

        /**
         * 2种情形
         * 1. create table $table_name as ($select_clause) [with data]
         * TODO: 2. create table $table_name as ($column_name $datatype, $column_name $datatype, ...)
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
         * 1. group by expr, expr, ...
         *
         * @param allNodes
         * @return
         */
        private static List<SqlNode> buildGroupByNodes(List<SqlNode> allNodes) {

            List<SqlNode> groupByNodes = new ArrayList<>();

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
                groupByNodes.add(exprNode);
            }
            return groupByNodes;
        }

        /**
         * 1种情形
         * 1. order by expr [asc|desc], expr [asc|desc], ...
         *
         * @param allNodes
         * @return
         */
        private static List<SqlNode> buildOrderExprNodes(List<SqlNode> allNodes) {

            List<SqlNode> orderExprs = new ArrayList<>();

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
                orderExprs.add(exprNode);
            }
            return orderExprs;
        }

        /**
         * 按逗号, 分隔nodes数组
         *       `(a+1) as a, b, f(x,g(y,z)) as c`
         * ==>  [`(a+1) as a`, `b`, `f(x) as c`]
         *
         * @param allNodes
         * @return
         */
        private static List<List<SqlNode>> splitByComma(List<SqlNode> allNodes, int startIndex, int stopIndex) {

//            if (allNodes == null || allNodes.size() == 0) return null;

            List<List<SqlNode>> tokensList = new ArrayList<>();

            int parenCount = 0;

            List<SqlNode> tokens = new ArrayList<>();
            for (int i = startIndex; i < stopIndex; i ++) {
                SqlNode n = allNodes.get(i);
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


        private static List<List<SqlNode>> splitByComma(List<SqlNode> allNodes) {
            return splitByComma(allNodes, 0, allNodes.size());
        }

        /**
         *
         * @param sqlTokens
         * @return
         */
        public static Node buildSimpleAST(List<SqlToken> sqlTokens) {

            //
            //更新Keywords
            //
            List<Token> tokens = new ArrayList<>();
            for (int i = 0; i < sqlTokens.size(); i ++) {
                SqlToken currToken = sqlTokens.get(i);

                boolean bKeyword = KEYWORDS.contains(currToken.getText().toUpperCase());

                tokens.add(new Token(currToken, bKeyword));
            }

            //
            //合并Keywords
            //
            List<Token> mergedTokens = new ArrayList<>();
            Token keywordToken = null;
            for (int i = 0; i < tokens.size(); i ++) {

                Token curToken = tokens.get(i);

                if (curToken.keyword) {

                    if (keywordToken == null) {
                        keywordToken = new Token(curToken);
                    } else {
                        if (canAppend(keywordToken, curToken)) {
                            keywordToken.append(curToken);
                        } else {
                            mergedTokens.add(keywordToken);
                            keywordToken = new Token(curToken);
                        }
                    }

                } else {
                    if (keywordToken != null) {
                        mergedTokens.add(keywordToken);
                        keywordToken = null;
                    }
                    mergedTokens.add(curToken);
                }
            }

            if (keywordToken != null) {
                mergedTokens.add(keywordToken);
            }

            //GO
            Statement statement = buildNestedStatement(mergedTokens);
            statement = buildNodes(statement);
            return statement;

        }


        /**
         * TODO: 对3+个连续keywords合并，算法需要优化 (准确性、性能)
         * 场景：
         * 1. 对于MySQL，支持一些特别的合并关键字，比如"`IF-NOT-EXISTS`", "`IF-EXISTS`"
         *
         * @param prev
         * @param next
         * @return
         */
        private static boolean canAppend(Token prev, Token next) {

            if (!prev.keyword || !next.keyword) return false;

            StringBuffer str = new StringBuffer();
            for (int i = 0; i < prev.sqlTokens.size(); i ++) {
                if (i > 0) str.append("-");
                str.append(prev.sqlTokens.get(i).getText());
            }
            str.append("-");
            for (int i = 0; i < next.sqlTokens.size(); i ++) {
                if (i > 0) str.append("-");
                str.append(next.sqlTokens.get(i).getText());
            }

            boolean canAppend = false;
            for (String keywords: MULTIPLE_KEYWORDS) {
                if (keywords.startsWith(str.toString().toUpperCase())) {
                    canAppend = true;
                    break;
                }
            }
            return canAppend;

        }
    }
}
