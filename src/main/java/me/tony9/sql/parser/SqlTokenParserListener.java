// Generated from /Users/Tony/Documents/develop/qparser/src/main/antlr/sql/SqlTokenParser.g4 by ANTLR 4.5.3
package me.tony9.sql.parser;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link SqlTokenParser}.
 */
public interface SqlTokenParserListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link SqlTokenParser#program}.
	 * @param ctx the parse tree
	 */
	void enterProgram(SqlTokenParser.ProgramContext ctx);
	/**
	 * Exit a parse tree produced by {@link SqlTokenParser#program}.
	 * @param ctx the parse tree
	 */
	void exitProgram(SqlTokenParser.ProgramContext ctx);
	/**
	 * Enter a parse tree produced by {@link SqlTokenParser#statements}.
	 * @param ctx the parse tree
	 */
	void enterStatements(SqlTokenParser.StatementsContext ctx);
	/**
	 * Exit a parse tree produced by {@link SqlTokenParser#statements}.
	 * @param ctx the parse tree
	 */
	void exitStatements(SqlTokenParser.StatementsContext ctx);
	/**
	 * Enter a parse tree produced by {@link SqlTokenParser#statement}.
	 * @param ctx the parse tree
	 */
	void enterStatement(SqlTokenParser.StatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link SqlTokenParser#statement}.
	 * @param ctx the parse tree
	 */
	void exitStatement(SqlTokenParser.StatementContext ctx);
}