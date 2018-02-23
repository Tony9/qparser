// Generated from /Users/Tony/Documents/develop/qparser/src/main/antlr/sql/SqlTokenLexer.g4 by ANTLR 4.5.3
package me.tony9.sql.parser;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.misc.*;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class SqlTokenLexer extends Lexer {
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
	public static String[] modeNames = {
		"DEFAULT_MODE"
	};

	public static final String[] ruleNames = {
		"LineTerminator", "OpenParen", "CloseParen", "OpenBrace", "CloseBrace", 
		"SemiColon", "Comma", "Assign", "Colon", "Dot", "Plus", "Minus", "Not", 
		"Multiply", "Divide", "LessThan", "MoreThan", "LessThanEquals", "GreaterThanEquals", 
		"NotEquals_1", "NotEquals_2", "Concat", "Identifier", "StringLiteral", 
		"WhiteSpaces", "MultiLineComment", "SingleLineComment", "UnexpectedCharacter"
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


	public SqlTokenLexer(CharStream input) {
		super(input);
		_interp = new LexerATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@Override
	public String getGrammarFileName() { return "SqlTokenLexer.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public String[] getModeNames() { return modeNames; }

	@Override
	public ATN getATN() { return _ATN; }

	public static final String _serializedATN =
		"\3\u0430\ud6d1\u8206\uad2d\u4417\uaef1\u8d80\uaadd\2\36\u00b0\b\1\4\2"+
		"\t\2\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4"+
		"\13\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22"+
		"\t\22\4\23\t\23\4\24\t\24\4\25\t\25\4\26\t\26\4\27\t\27\4\30\t\30\4\31"+
		"\t\31\4\32\t\32\4\33\t\33\4\34\t\34\4\35\t\35\3\2\3\2\3\2\3\2\3\3\3\3"+
		"\3\4\3\4\3\5\3\5\3\6\3\6\3\7\3\7\3\b\3\b\3\t\3\t\3\n\3\n\3\13\3\13\3\f"+
		"\3\f\3\r\3\r\3\16\3\16\3\17\3\17\3\20\3\20\3\21\3\21\3\22\3\22\3\23\3"+
		"\23\3\23\3\24\3\24\3\24\3\25\3\25\3\25\3\26\3\26\3\26\3\27\3\27\3\27\3"+
		"\30\5\30p\n\30\3\30\5\30s\n\30\3\30\7\30v\n\30\f\30\16\30y\13\30\3\30"+
		"\3\30\5\30}\n\30\3\30\5\30\u0080\n\30\3\31\3\31\3\31\3\31\3\31\3\31\7"+
		"\31\u0088\n\31\f\31\16\31\u008b\13\31\3\31\3\31\3\32\6\32\u0090\n\32\r"+
		"\32\16\32\u0091\3\32\3\32\3\33\3\33\3\33\3\33\7\33\u009a\n\33\f\33\16"+
		"\33\u009d\13\33\3\33\3\33\3\33\3\33\3\33\3\34\3\34\3\34\3\34\7\34\u00a8"+
		"\n\34\f\34\16\34\u00ab\13\34\3\34\3\34\3\35\3\35\3\u009b\2\36\3\3\5\4"+
		"\7\5\t\6\13\7\r\b\17\t\21\n\23\13\25\f\27\r\31\16\33\17\35\20\37\21!\22"+
		"#\23%\24\'\25)\26+\27-\30/\31\61\32\63\33\65\34\67\359\36\3\2\6\5\2\f"+
		"\f\17\17\u202a\u202b\7\2\60\60\62;C\\aac|\4\2))^^\6\2\13\13\r\16\"\"\u00a2"+
		"\u00a2\u00ba\2\3\3\2\2\2\2\5\3\2\2\2\2\7\3\2\2\2\2\t\3\2\2\2\2\13\3\2"+
		"\2\2\2\r\3\2\2\2\2\17\3\2\2\2\2\21\3\2\2\2\2\23\3\2\2\2\2\25\3\2\2\2\2"+
		"\27\3\2\2\2\2\31\3\2\2\2\2\33\3\2\2\2\2\35\3\2\2\2\2\37\3\2\2\2\2!\3\2"+
		"\2\2\2#\3\2\2\2\2%\3\2\2\2\2\'\3\2\2\2\2)\3\2\2\2\2+\3\2\2\2\2-\3\2\2"+
		"\2\2/\3\2\2\2\2\61\3\2\2\2\2\63\3\2\2\2\2\65\3\2\2\2\2\67\3\2\2\2\29\3"+
		"\2\2\2\3;\3\2\2\2\5?\3\2\2\2\7A\3\2\2\2\tC\3\2\2\2\13E\3\2\2\2\rG\3\2"+
		"\2\2\17I\3\2\2\2\21K\3\2\2\2\23M\3\2\2\2\25O\3\2\2\2\27Q\3\2\2\2\31S\3"+
		"\2\2\2\33U\3\2\2\2\35W\3\2\2\2\37Y\3\2\2\2![\3\2\2\2#]\3\2\2\2%_\3\2\2"+
		"\2\'b\3\2\2\2)e\3\2\2\2+h\3\2\2\2-k\3\2\2\2/o\3\2\2\2\61\u0081\3\2\2\2"+
		"\63\u008f\3\2\2\2\65\u0095\3\2\2\2\67\u00a3\3\2\2\29\u00ae\3\2\2\2;<\t"+
		"\2\2\2<=\3\2\2\2=>\b\2\2\2>\4\3\2\2\2?@\7*\2\2@\6\3\2\2\2AB\7+\2\2B\b"+
		"\3\2\2\2CD\7}\2\2D\n\3\2\2\2EF\7\177\2\2F\f\3\2\2\2GH\7=\2\2H\16\3\2\2"+
		"\2IJ\7.\2\2J\20\3\2\2\2KL\7?\2\2L\22\3\2\2\2MN\7<\2\2N\24\3\2\2\2OP\7"+
		"\60\2\2P\26\3\2\2\2QR\7-\2\2R\30\3\2\2\2ST\7/\2\2T\32\3\2\2\2UV\7#\2\2"+
		"V\34\3\2\2\2WX\7,\2\2X\36\3\2\2\2YZ\7\61\2\2Z \3\2\2\2[\\\7>\2\2\\\"\3"+
		"\2\2\2]^\7@\2\2^$\3\2\2\2_`\7>\2\2`a\7?\2\2a&\3\2\2\2bc\7@\2\2cd\7?\2"+
		"\2d(\3\2\2\2ef\7>\2\2fg\7@\2\2g*\3\2\2\2hi\7#\2\2ij\7?\2\2j,\3\2\2\2k"+
		"l\7~\2\2lm\7~\2\2m.\3\2\2\2np\7%\2\2on\3\2\2\2op\3\2\2\2pr\3\2\2\2qs\7"+
		"}\2\2rq\3\2\2\2rs\3\2\2\2sw\3\2\2\2tv\t\3\2\2ut\3\2\2\2vy\3\2\2\2wu\3"+
		"\2\2\2wx\3\2\2\2x|\3\2\2\2yw\3\2\2\2z{\7\60\2\2{}\7,\2\2|z\3\2\2\2|}\3"+
		"\2\2\2}\177\3\2\2\2~\u0080\7\177\2\2\177~\3\2\2\2\177\u0080\3\2\2\2\u0080"+
		"\60\3\2\2\2\u0081\u0089\7)\2\2\u0082\u0083\7^\2\2\u0083\u0088\13\2\2\2"+
		"\u0084\u0085\7)\2\2\u0085\u0088\7)\2\2\u0086\u0088\n\4\2\2\u0087\u0082"+
		"\3\2\2\2\u0087\u0084\3\2\2\2\u0087\u0086\3\2\2\2\u0088\u008b\3\2\2\2\u0089"+
		"\u0087\3\2\2\2\u0089\u008a\3\2\2\2\u008a\u008c\3\2\2\2\u008b\u0089\3\2"+
		"\2\2\u008c\u008d\7)\2\2\u008d\62\3\2\2\2\u008e\u0090\t\5\2\2\u008f\u008e"+
		"\3\2\2\2\u0090\u0091\3\2\2\2\u0091\u008f\3\2\2\2\u0091\u0092\3\2\2\2\u0092"+
		"\u0093\3\2\2\2\u0093\u0094\b\32\2\2\u0094\64\3\2\2\2\u0095\u0096\7\61"+
		"\2\2\u0096\u0097\7,\2\2\u0097\u009b\3\2\2\2\u0098\u009a\13\2\2\2\u0099"+
		"\u0098\3\2\2\2\u009a\u009d\3\2\2\2\u009b\u009c\3\2\2\2\u009b\u0099\3\2"+
		"\2\2\u009c\u009e\3\2\2\2\u009d\u009b\3\2\2\2\u009e\u009f\7,\2\2\u009f"+
		"\u00a0\7\61\2\2\u00a0\u00a1\3\2\2\2\u00a1\u00a2\b\33\2\2\u00a2\66\3\2"+
		"\2\2\u00a3\u00a4\7/\2\2\u00a4\u00a5\7/\2\2\u00a5\u00a9\3\2\2\2\u00a6\u00a8"+
		"\n\2\2\2\u00a7\u00a6\3\2\2\2\u00a8\u00ab\3\2\2\2\u00a9\u00a7\3\2\2\2\u00a9"+
		"\u00aa\3\2\2\2\u00aa\u00ac\3\2\2\2\u00ab\u00a9\3\2\2\2\u00ac\u00ad\b\34"+
		"\2\2\u00ad8\3\2\2\2\u00ae\u00af\13\2\2\2\u00af:\3\2\2\2\r\2orw|\177\u0087"+
		"\u0089\u0091\u009b\u00a9\3\2\3\2";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}