lexer grammar SqlTokenLexer;

options {
}

/// Line Terminators
LineTerminator:                 [\r\n\u2028\u2029] -> channel(HIDDEN);

OpenParen:                      '(';
CloseParen:                     ')';
OpenBrace:                      '{';
CloseBrace:                     '}';
SemiColon:                      ';';
Comma:                          ',';
Assign:                         '=';
Colon:                          ':';
Dot:                            '.';
Plus:                           '+';
Minus:                          '-';
Not:                            '!';
Multiply:                       '*';
Divide:                         '/';
LessThan:                       '<';
MoreThan:                       '>';
LessThanEquals:                 '<=';
GreaterThanEquals:              '>=';
NotEquals_1:                    '<>';
NotEquals_2:                    '!=';
Concat:                         '||';


/// Identifier Names and Identifiers

Identifier:                     '#'?'{'?[a-zA-Z0-9_.]*'.*'?'}'?;

/// String Literals
StringLiteral:                  '\'' ('\\'. | '\'\'' | ~('\'' | '\\'))* '\'';

WhiteSpaces:                    [\t\u000B\u000C\u0020\u00A0]+ -> channel(HIDDEN);

/// Comments

MultiLineComment:               '/*' .*? '*/' -> channel(HIDDEN);
SingleLineComment:              '--' ~[\r\n\u2028\u2029]* -> channel(HIDDEN);
UnexpectedCharacter:            . ;
