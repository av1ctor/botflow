package com.robotikflow.core.services.formula.scanner;

import java_cup.runtime.*;
import com.robotikflow.core.services.formula.parser.ParserSym;

%%

%public
%class Scanner

%unicode

%column

%cup

%{
  StringBuilder string = new StringBuilder();
  
  private Symbol symbol(int type) {
    return new Symbol(type, yyline+1, yycolumn+1);
  }

  private Symbol symbol(int type, Object value) {
    return new Symbol(type, yyline+1, yycolumn+1, value);
  }

  private long hexToLong(int start, int end, int radix) {
    long result = 0;
    long digit;

    for (int i = start; i < end; i++) {
      digit  = Character.digit(yycharat(i),radix);
      result*= radix;
      result+= digit;
    }

    return result;
  }
%}

LineTerminator = \r|\n|\r\n
InputCharacter = [^\r\n]

WhiteSpace = {LineTerminator} | [ \t\f]

Comment = {TraditionalComment} | {EndOfLineComment}

TraditionalComment = "/*" [^*] ~"*/" | "/*" "*"+ "/"
EndOfLineComment = "//" {InputCharacter}* {LineTerminator}?

Identifier = [:jletter:][:jletterdigit:]*

/* integer literals */
DecIntegerLiteral = 0 | [1-9][0-9]*
DecLongLiteral    = {DecIntegerLiteral} [lL]

HexIntegerLiteral = 0 [xX] 0* {HexDigit} {1,8}
HexLongLiteral    = 0 [xX] 0* {HexDigit} {1,16} [lL]
HexDigit          = [0-9a-fA-F]

DoubleLiteral = ({FLit1}|{FLit2}|{FLit3}) {Exponent}?

FLit1    = [0-9]+ \. [0-9]* 
FLit2    = \. [0-9]+ 
FLit3    = [0-9]+ 
Exponent = [eE] [+-]? [0-9]+

StringCharacter   = [^\r\n\"\\]
StringCharacterSQ = [^\r\n'\\]

%state STRING, STRING_SQ

%%

<YYINITIAL> {

  /* keywords */
  "true"                        { return symbol(ParserSym.BOOLEAN_LIT, true); }
  "false"                       { return symbol(ParserSym.BOOLEAN_LIT, false); }
  "null"                        { return symbol(ParserSym.NULL_LIT); }
  
  /* separators */
  "("                            { return symbol(ParserSym.LPAREN); }
  ")"                            { return symbol(ParserSym.RPAREN); }
  "{"                            { return symbol(ParserSym.LBRACE); }
  "}"                            { return symbol(ParserSym.RBRACE); }
  "["                            { return symbol(ParserSym.LBRACK); }
  "]"                            { return symbol(ParserSym.RBRACK); }
  ";"                            { return symbol(ParserSym.SEMICOLON); }
  ","                            { return symbol(ParserSym.COMMA); }
  "."                            { return symbol(ParserSym.DOT); }
  
  /* operators */
  "="                            { return symbol(ParserSym.EQ); }
  ">"                            { return symbol(ParserSym.GT); }
  "<"                            { return symbol(ParserSym.LT); }
  "!"                            { return symbol(ParserSym.NOT); }
  "?"                            { return symbol(ParserSym.QUESTION); }
  ":"                            { return symbol(ParserSym.COLON); }
  "==="                          { return symbol(ParserSym.EQEQ); }
  "=="                           { return symbol(ParserSym.EQEQ); }
  "<="                           { return symbol(ParserSym.LTEQ); }
  ">="                           { return symbol(ParserSym.GTEQ); }
  "<>"                           { return symbol(ParserSym.NOTEQ); }
  "!="                           { return symbol(ParserSym.NOTEQ); }
  "&&"                           { return symbol(ParserSym.ANDAND); }
  "||"                           { return symbol(ParserSym.OROR); }
  "+"                            { return symbol(ParserSym.PLUS); }
  "-"                            { return symbol(ParserSym.MINUS); }
  "*"                            { return symbol(ParserSym.MULT); }
  "/"                            { return symbol(ParserSym.DIV); }
  "&"                            { return symbol(ParserSym.AND); }
  "|"                            { return symbol(ParserSym.OR); }
  "^"                            { return symbol(ParserSym.POW); }
  "%"                            { return symbol(ParserSym.MOD); }
  "<<"                           { return symbol(ParserSym.LSHIFT); }
  ">>"                           { return symbol(ParserSym.RSHIFT); }
  
  /* string literal */
  \"                             { yybegin(STRING); string.setLength(0); }
  "'"                            { yybegin(STRING_SQ); string.setLength(0); }

  /* numeric literals */

  /* This is matched together with the minus, because the number is too big to 
     be represented by a positive integer. */
  "-2147483648"                  { return symbol(ParserSym.INTEGER_LIT, Integer.valueOf(Integer.MIN_VALUE)); }
  
  {DecIntegerLiteral}            { return symbol(ParserSym.INTEGER_LIT, Integer.valueOf(yytext())); }
  {DecLongLiteral}               { return symbol(ParserSym.INTEGER_LIT, Long.parseLong(yytext().substring(0,yylength()-1))); }
  
  {HexIntegerLiteral}            { return symbol(ParserSym.INTEGER_LIT, (int)hexToLong(2, yylength(), 16)); }
  {HexLongLiteral}               { return symbol(ParserSym.INTEGER_LIT, hexToLong(2, yylength()-1, 16)); }
 
  {DoubleLiteral}                { return symbol(ParserSym.FLOATING_POINT_LIT, Double.parseDouble(yytext())); }
  
  /* comments */
  {Comment}                      { /* ignore */ }

  /* whitespace */
  {WhiteSpace}                   { /* ignore */ }

  /* identifiers */ 
  {Identifier}                   { return symbol(ParserSym.IDENTIFIER, yytext()); }  
}

<STRING> {
  \"                             { yybegin(YYINITIAL); return symbol(ParserSym.STRING_LIT, string.toString()); }
  
  {StringCharacter}+             { string.append( yytext() ); }
  
  /* escape sequences */
  "\\b"                          { string.append( '\b' ); }
  "\\t"                          { string.append( '\t' ); }
  "\\n"                          { string.append( '\n' ); }
  "\\f"                          { string.append( '\f' ); }
  "\\r"                          { string.append( '\r' ); }
  "\\\""                         { string.append( '\"' ); }
  "\\'"                          { string.append( '\'' ); }
  "\\\\"                         { string.append( '\\' ); }
  /* error cases */
  \\.                            { throw new RuntimeException("Illegal escape sequence \""+yytext()+"\""); }
  {LineTerminator}               { throw new RuntimeException("Unterminated string at end of line"); }
}

<STRING_SQ> {
  "'"                            { yybegin(YYINITIAL); return symbol(ParserSym.STRING_LIT, string.toString()); }
  
  {StringCharacterSQ}+           { string.append( yytext() ); }
  
  /* escape sequences */
  "\\b"                          { string.append( '\b' ); }
  "\\t"                          { string.append( '\t' ); }
  "\\n"                          { string.append( '\n' ); }
  "\\f"                          { string.append( '\f' ); }
  "\\r"                          { string.append( '\r' ); }
  "\\\""                         { string.append( '\"' ); }
  "\\'"                          { string.append( '\'' ); }
  "\\\\"                         { string.append( '\\' ); }
  /* error cases */
  \\.                            { throw new RuntimeException("Illegal escape sequence \""+yytext()+"\""); }
  {LineTerminator}               { throw new RuntimeException("Unterminated string at end of line"); }
}

/* error fallback */
[^]                              { throw new RuntimeException("Illegal character \""+yytext()+
                                                              "\" at line "+yyline+", column "+yycolumn); }
<<EOF>>                          { return symbol(ParserSym.EOF); }