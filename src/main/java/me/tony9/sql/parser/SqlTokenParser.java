// Generated from /Users/Tony/Documents/develop/qparser/src/main/antlr/sql/SqlTokenParser.g4 by ANTLR 4.5.3
package me.tony9.sql.parser;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class SqlTokenParser extends Parser {
	static { RuntimeMetaData.checkVersion("4.5.3", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		LineTerminator=1, OpenParen=2, CloseParen=3, OpenBrace=4, CloseBrace=5, 
		SemiColon=6, Comma=7, Assign=8, Colon=9, Dot=10, Plus=11, Minus=12, Not=13, 
		Multiply=14, Divide=15, LessThan=16, MoreThan=17, LessThanEquals=18, GreaterThanEquals=19, 
		NotEquals_1=20, NotEquals_2=21, Concat=22, Identifier=23, StringLiteral=24, 
		WhiteSpaces=25, MultiLineComment=26, SingleLineComment=27, UnexpectedCharacter=28;
	public static final int
		RULE_program = 0, RULE_statements = 1, RULE_statement = 2;
	public static final String[] ruleNames = {
		"program", "statements", "statement"
	};

	private static final String[] _LITERAL_NAMES = {
		null, null, "'('", "')'", "'{'", "'}'", "';'", "','", "'='", "':'", "'.'", 
		"'+'", "'-'", "'!'", "'*'", "'/'", "'<'", "'>'", "'<='", "'>='", "'<>'", 
		"'!='", "'||'"
	};
	private static final String[] _SYMBOLIC_NAMES = {
		null, "LineTerminator", "OpenParen", "CloseParen", "OpenBrace", "CloseBrace", 
		"SemiColon", "Comma", "Assign", "Colon", "Dot", "Plus", "Minus", "Not", 
		"Multiply", "Divide", "LessThan", "MoreThan", "LessThanEquals", "GreaterThanEquals", 
		"NotEquals_1", "NotEquals_2", "Concat", "Identifier", "StringLiteral", 
		"WhiteSpaces", "MultiLineComment", "SingleLineComment", "UnexpectedCharacter"
	};
	public static final Vocabulary VOCABULARY = new VocabularyImpl(_LITERAL_NAMES, _SYMBOLIC_NAMES);

	/**
	 * @deprecated Use {@link #VOCABULARY} instead.
	 */
	@Deprecated
	public static final String[] tokenNames;
	static {
		tokenNames = new String[_SYMBOLIC_NAMES.length];
		for (int i = 0; i < tokenNames.length; i++) {
			tokenNames[i] = VOCABULARY.getLiteralName(i);
			if (tokenNames[i] == null) {
				tokenNames[i] = VOCABULARY.getSymbolicName(i);
			}

			if (tokenNames[i] == null) {
				tokenNames[i] = "<INVALID>";
			}
		}
	}

	@Override
	@Deprecated
	public String[] getTokenNames() {
		return tokenNames;
	}

	@Override

	public Vocabulary getVocabulary() {
		return VOCABULARY;
	}

	@Override
	public String getGrammarFileName() { return "SqlTokenParser.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public ATN getATN() { return _ATN; }

	public SqlTokenParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}
	public static class ProgramContext extends ParserRuleContext {
		public TerminalNode EOF() { return getToken(SqlTokenParser.EOF, 0); }
		public StatementsContext statements() {
			return getRuleContext(StatementsContext.class,0);
		}
		public ProgramContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_program; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlTokenParserListener ) ((SqlTokenParserListener)listener).enterProgram(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlTokenParserListener ) ((SqlTokenParserListener)listener).exitProgram(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlTokenParserVisitor ) return ((SqlTokenParserVisitor<? extends T>)visitor).visitProgram(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ProgramContext program() throws RecognitionException {
		ProgramContext _localctx = new ProgramContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_program);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(7);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,0,_ctx) ) {
			case 1:
				{
				setState(6);
				statements();
				}
				break;
			}
			setState(9);
			match(EOF);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class StatementsContext extends ParserRuleContext {
		public List<StatementContext> statement() {
			return getRuleContexts(StatementContext.class);
		}
		public StatementContext statement(int i) {
			return getRuleContext(StatementContext.class,i);
		}
		public List<TerminalNode> SemiColon() { return getTokens(SqlTokenParser.SemiColon); }
		public TerminalNode SemiColon(int i) {
			return getToken(SqlTokenParser.SemiColon, i);
		}
		public StatementsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_statements; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlTokenParserListener ) ((SqlTokenParserListener)listener).enterStatements(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlTokenParserListener ) ((SqlTokenParserListener)listener).exitStatements(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlTokenParserVisitor ) return ((SqlTokenParserVisitor<? extends T>)visitor).visitStatements(this);
			else return visitor.visitChildren(this);
		}
	}

	public final StatementsContext statements() throws RecognitionException {
		StatementsContext _localctx = new StatementsContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_statements);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(16);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << OpenParen) | (1L << CloseParen) | (1L << SemiColon) | (1L << Comma) | (1L << Assign) | (1L << Plus) | (1L << Minus) | (1L << Multiply) | (1L << Divide) | (1L << LessThan) | (1L << MoreThan) | (1L << LessThanEquals) | (1L << GreaterThanEquals) | (1L << NotEquals_1) | (1L << NotEquals_2) | (1L << Concat) | (1L << Identifier) | (1L << StringLiteral) | (1L << MultiLineComment) | (1L << SingleLineComment))) != 0)) {
				{
				{
				setState(11);
				statement();
				setState(12);
				match(SemiColon);
				}
				}
				setState(18);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class StatementContext extends ParserRuleContext {
		public List<TerminalNode> MultiLineComment() { return getTokens(SqlTokenParser.MultiLineComment); }
		public TerminalNode MultiLineComment(int i) {
			return getToken(SqlTokenParser.MultiLineComment, i);
		}
		public List<TerminalNode> SingleLineComment() { return getTokens(SqlTokenParser.SingleLineComment); }
		public TerminalNode SingleLineComment(int i) {
			return getToken(SqlTokenParser.SingleLineComment, i);
		}
		public List<TerminalNode> Identifier() { return getTokens(SqlTokenParser.Identifier); }
		public TerminalNode Identifier(int i) {
			return getToken(SqlTokenParser.Identifier, i);
		}
		public List<TerminalNode> StringLiteral() { return getTokens(SqlTokenParser.StringLiteral); }
		public TerminalNode StringLiteral(int i) {
			return getToken(SqlTokenParser.StringLiteral, i);
		}
		public StatementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_statement; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlTokenParserListener ) ((SqlTokenParserListener)listener).enterStatement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlTokenParserListener ) ((SqlTokenParserListener)listener).exitStatement(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlTokenParserVisitor ) return ((SqlTokenParserVisitor<? extends T>)visitor).visitStatement(this);
			else return visitor.visitChildren(this);
		}
	}

	public final StatementContext statement() throws RecognitionException {
		StatementContext _localctx = new StatementContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_statement);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(22);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << OpenParen) | (1L << CloseParen) | (1L << Comma) | (1L << Assign) | (1L << Plus) | (1L << Minus) | (1L << Multiply) | (1L << Divide) | (1L << LessThan) | (1L << MoreThan) | (1L << LessThanEquals) | (1L << GreaterThanEquals) | (1L << NotEquals_1) | (1L << NotEquals_2) | (1L << Concat) | (1L << Identifier) | (1L << StringLiteral) | (1L << MultiLineComment) | (1L << SingleLineComment))) != 0)) {
				{
				{
				setState(19);
				_la = _input.LA(1);
				if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << OpenParen) | (1L << CloseParen) | (1L << Comma) | (1L << Assign) | (1L << Plus) | (1L << Minus) | (1L << Multiply) | (1L << Divide) | (1L << LessThan) | (1L << MoreThan) | (1L << LessThanEquals) | (1L << GreaterThanEquals) | (1L << NotEquals_1) | (1L << NotEquals_2) | (1L << Concat) | (1L << Identifier) | (1L << StringLiteral) | (1L << MultiLineComment) | (1L << SingleLineComment))) != 0)) ) {
				_errHandler.recoverInline(this);
				} else {
					consume();
				}
				}
				}
				setState(24);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static final String _serializedATN =
		"\3\u0430\ud6d1\u8206\uad2d\u4417\uaef1\u8d80\uaadd\3\36\34\4\2\t\2\4\3"+
		"\t\3\4\4\t\4\3\2\5\2\n\n\2\3\2\3\2\3\3\3\3\3\3\7\3\21\n\3\f\3\16\3\24"+
		"\13\3\3\4\7\4\27\n\4\f\4\16\4\32\13\4\3\4\2\2\5\2\4\6\2\3\7\2\4\5\t\n"+
		"\r\16\20\32\34\35\33\2\t\3\2\2\2\4\22\3\2\2\2\6\30\3\2\2\2\b\n\5\4\3\2"+
		"\t\b\3\2\2\2\t\n\3\2\2\2\n\13\3\2\2\2\13\f\7\2\2\3\f\3\3\2\2\2\r\16\5"+
		"\6\4\2\16\17\7\b\2\2\17\21\3\2\2\2\20\r\3\2\2\2\21\24\3\2\2\2\22\20\3"+
		"\2\2\2\22\23\3\2\2\2\23\5\3\2\2\2\24\22\3\2\2\2\25\27\t\2\2\2\26\25\3"+
		"\2\2\2\27\32\3\2\2\2\30\26\3\2\2\2\30\31\3\2\2\2\31\7\3\2\2\2\32\30\3"+
		"\2\2\2\5\t\22\30";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}