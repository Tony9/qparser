parser grammar SqlTokenParser;

options {
    language=Java;
    tokenVocab=SqlTokenLexer;
}

program
    : statements? EOF
    ;

statements
    : (statement SemiColon)*
    ;

statement
    : ( MultiLineComment
      | SingleLineComment
      | Identifier
      | StringLiteral
      | ',' | '(' | ')'
      | '+' | '-' | '*' | '/'
      | '>' | '>=' | '<' | '<=' | '=' | '<>' | '!='
      | '||'
      ) *
    ;
