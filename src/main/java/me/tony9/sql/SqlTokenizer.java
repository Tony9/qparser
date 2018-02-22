package me.tony9.sql;

import me.tony9.sql.parser.SqlTokenLexer;
import me.tony9.sql.parser.SqlTokenParser;
import me.tony9.sql.parser.SqlTokenParserBaseVisitor;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.TerminalNodeImpl;

import java.util.ArrayList;
import java.util.List;

import static me.tony9.sql.parser.SqlTokenParser.MultiLineComment;

/**
 * Created by Tony on 2017/12/19.
 */
public class SqlTokenizer {

    public List<SqlToken> split(String sql) {

        ANTLRInputStream in = new ANTLRInputStream(sql);
        SqlTokenLexer lexer = new SqlTokenLexer(in);
        CommonTokenStream tokens = new CommonTokenStream(lexer);

        SqlTokenParser parser = new SqlTokenParser(tokens);

        SqlTokenParser.StatementContext tree = parser.statement();

        SqlTokenVisitor visitor = new SqlTokenVisitor();
        List<SqlToken> tokenList = visitor.visitStatement(tree);

        return tokenList;
    }

    private static class SqlTokenVisitor extends SqlTokenParserBaseVisitor<List<SqlToken>> {

        public SqlTokenVisitor() {

        }

//        @Override
//        public List<SqlToken> visitProgram(SqlTokenParser.ProgramContext ctx) {
//            List<SqlToken> tokenList = new ArrayList<>();
//            tokenList.addAll(visitStatements(ctx.statements()));
//            return tokenList;
//        }
//
//        @Override
//        public List<SqlToken> visitStatements(SqlTokenParser.StatementsContext ctx) {
//            List<SqlToken> tokenList = new ArrayList<>();
//            for (SqlTokenParser.StatementContext statement : ctx.statement()) {
//                tokenList.addAll(visitStatement(statement));
//            }
//            return tokenList;
//        }

        /**
         * statement
         *    : ( MultiLineComment
         *    | SingleLineComment
         *    | Identifier
         *    | StringLiteral
         *    | ',' | '(' | ')'
         *    | '+' | '-' | '*' | '/'
         *    | '>' | '>=' | '<' | '<=' | '=' | '<>' | '!='
         *    | '||'
         *    ) *
         *    ;
         * @param ctx
         * @return
         */
        @Override
        public List<SqlToken> visitStatement(SqlTokenParser.StatementContext ctx) {

            List<SqlToken> tokenList = new ArrayList<>();
            int count = ctx.getChildCount();
            for (int i = 0; i < count; i ++) {
                TerminalNodeImpl t = (TerminalNodeImpl)ctx.getChild(i);
                tokenList.add(new SqlToken(t.getText(), t.getSymbol().getStartIndex(), t.getSymbol().getStopIndex()+1));
            }
            return tokenList;
        }
    }
}
