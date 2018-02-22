// Generated from /Users/Tony/Documents/develop/qparser/src/main/antlr/sql/SqlTokenParser.g4 by ANTLR 4.5.3
package me.tony9.sql.parser;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link SqlTokenParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface SqlTokenParserVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link SqlTokenParser#program}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitProgram(SqlTokenParser.ProgramContext ctx);
	/**
	 * Visit a parse tree produced by {@link SqlTokenParser#statements}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStatements(SqlTokenParser.StatementsContext ctx);
	/**
	 * Visit a parse tree produced by {@link SqlTokenParser#statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStatement(SqlTokenParser.StatementContext ctx);
}