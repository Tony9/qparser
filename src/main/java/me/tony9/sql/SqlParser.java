package me.tony9.sql;

import me.tony9.util.tree.Node;

import java.util.*;

public class SqlParser {

    private static Set<String> KEYWORDS;
    static {
        String[] statements = new String[] {
                //Table
                "CREATE TABLE WITH DATA",
                "SELECT FROM [LEFT|RIGHT|INNER|OUTER] JOIN ON WHERE GROUP BY HAVING ORDER BY LIMIT UNION [ALL]",
                "INSERT INTO VALUES",
                "DELETE FROM",
                "DROP TABLE",
                //Index
                "CREATE INDEX ON",
                "DROP INDEX",
                //Expression
                "AND", "OR", "NOT"
        };

        KEYWORDS = new HashSet<>();
        for (int i = 0; i < statements.length; i ++) {
            for (String w : statements[i].split("[\\s\\[\\]\\|]+")) {
                KEYWORDS.add(w);
            }
        }
    }

    private enum SqlNodeType {

        //Statement
        CREATE("CREATE"),
        INSERT("INSERT"),
        UPDATE("UPDATE"),
        DELETE("DELETE"),
        SELECT("SELECT"),
        //Token
        TERMINAL("TERMINAL")
        ;

        private String type;

        private SqlNodeType(String type) {
            this.type = type;
        }

    }


    private static class SqlNode extends Node<String> {

        protected SqlNodeType type;
    }

    private static class Token extends SqlNode {

        private SqlToken sqlToken;
        protected int tokenIndex;//node tokenIndex in all tokens
        private boolean keyword;

        public Token(int tokenIndex, SqlToken sqlToken, boolean keyword) {
            this.tokenIndex = tokenIndex;
            this.sqlToken = sqlToken;
            this.keyword = keyword;
        }

//        public String toString() {
//            String t = this.sqlToken.getText();
//            return this.keyword? "`"+t.toUpperCase()+"`":t;
//        }

        public int getTokenIndex() {
            return tokenIndex;
        }


        public String toString() {
            String t = this.sqlToken.getText();
            return this.keyword? "`"+t.toUpperCase()+"`":t;
        }
    }

    private static class Expression extends SqlNode {

        public String toString() {

            StringBuffer text = null;
            if (this.getParent() == null) {
                text = new StringBuffer("`EXPRESSION`");
            } else if (this.getParent().toString().equals("`SELECT`")) {
                text = new StringBuffer("`COLUMN`");
            } else if (this.getParent().toString().equals("`FROM`") || this.getParent().toString().equals("`JOIN`")) {
                text = new StringBuffer("`TABLE`");
            } else {
                text = new StringBuffer("`EXPRESSION`");
            }

            return text.toString();
        }
    }

    private static class Statement extends SqlNode {

        public String toString() {

//            String text = this.getChildren().get(0).toString();
//            if ("`SELECT`".equals(text)) {
//                return (this.getParent() == null)?"`QUERY`":"`SUB-QUERY`";
//            } else {
//                return "`UPDATE`";
//            }
            return "`STATEMENT`";
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

            List<Interval<Integer>> intervals = findAllParenPairs(sqlTokens);
            Set<Integer> mapStart = new HashSet<>();
            Set<Integer> mapStop = new HashSet<>();
            Map<Integer, Integer> mapStartStop = new HashMap<>();
            for (Interval<Integer> interval : intervals) {

                SqlToken sqlToken = sqlTokens.get(interval.start + 1);
                if (TOKEN_SELECT.equals(sqlToken.getText().toUpperCase())) {
                    mapStart.add(interval.start);
                    mapStop.add(interval.stop);
                    mapStartStop.put(interval.stop, interval.start);
                }
            }

            //create statement
            LinkedList<Node> stack = new LinkedList<>();

            for (int i = 0; i < sqlTokens.size(); i++) {

                SqlToken currToken = sqlTokens.get(i);
                boolean bKeyword = KEYWORDS.contains(currToken.getText().toUpperCase());
                if (mapStart.contains(i)) {

                    stack.push(new Token(i, currToken, bKeyword));

                } else if (mapStop.contains(i)) {

                    Statement s = new Statement();

                    //查找与之匹配的leftParen
                    Node t = null;
                    while (true) {
                        t = stack.pop();
                        if (t instanceof Token && mapStart.contains(((Token) t).getTokenIndex())) {  //如果出现在mapStart中，就要跳出while循环
                            break;
                        } else {
                            s.addFirst(t);
                        }
                    }

                    stack.push(s);

                } else {
                    stack.push(new Token(i, currToken, bKeyword));
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
         * 1. 把SQL关键字后面的Node作为关键字的子节点
         * 2. 合并同级Token之间的字符
         *
         * 注意，必须传入Nested Statement。
         *
         * @param nestedStatement
         * @return
         */
        private static Statement mergeNodes(Statement nestedStatement) {

            Statement newStatement = new Statement();

            LinkedList<Node> nodes = new LinkedList<>();
            Token keywordToken = null;

            for (int i = 0; i < nestedStatement.getChildren().size(); i ++) {
                Node node = nestedStatement.getChildren().get(i);
                if (node instanceof Token && ((Token) node).keyword) {
                    if (keywordToken != null) {
                        List<Expression> exprs = buildExpression(nodes);
                        keywordToken.addAll(exprs);
                        newStatement.addLast(keywordToken);
                        nodes = new LinkedList<>();
                    }

                    Token n = (Token) node;
                    keywordToken = new Token(n.tokenIndex, n.sqlToken, n.keyword);
                } else if (node instanceof Statement) {
                    Statement subStatement = mergeNodes((Statement)node); //递归
                    nodes.add(subStatement);
                } else {
                    nodes.add(node);
                }
            }

            //add remain all
            if (keywordToken != null) {
                List<Expression> exprs = buildExpression(nodes);
                keywordToken.addAll(exprs);
                newStatement.addLast(keywordToken);
            }

            return newStatement;
        }


        /**
         * 按COMMA, 分隔nodes数组
         *       `(a+1) as a, b, f(x,g(y,z)) as c`
         * ==>  [`(a+1) as a`, `b`, `f(x) as c`]
         *
         * @param nodes
         * @return
         */
        private static List<Expression> buildExpression(List<Node> nodes) {

            if (nodes == null || nodes.size() == 0) return null;

            List<Expression> exprs = new ArrayList<>();

            int parenCount = 0;

            Expression expr = new Expression();
            for (Node n: nodes) {

                if (LEFT_PAREN.equals(n.toString())) {
                    parenCount ++;
                    expr.addLast(n);
                } else if (RIGHT_PAREN.equals(n.toString())) {
                    parenCount --;
                    expr.addLast(n);
                } else if (COMMA.equals(n.toString())) {
                    if (parenCount == 0) {
                        exprs.add(expr);
                        expr = new Expression();
                    } else {
                        expr.addLast(n);
                    }
                } else {
                    expr.addLast(n);
                }
            }

            //last one
            exprs.add(expr);

            return exprs;
        }

        /**
         *
         * @param sqlTokens
         * @return
         */
        public static Node buildSimpleAST(List<SqlToken> sqlTokens) {

            Statement statement = buildNestedStatement(sqlTokens);
            statement = mergeNodes(statement);
            return statement;
        }
    }
}
