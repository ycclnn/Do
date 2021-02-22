package com.zhennan.doo;

class Token {
	 //type of token
	 final TokenType type;
	 //As it appear in the input
	  final String lexeme;
	  //the value, ex: lexeme is "4", representing literal "4.0".
	  final Object literal;
	  //line of token
	  final int line; 

	  //token constructor
	  Token(TokenType type, String lexeme, Object literal, int line) {
	    this.type = type;
	    this.lexeme = lexeme;
	    this.literal = literal;
	    this.line = line;
	  }
	  
	  //override the object's toString method to display meaningful sentence.
	  @Override
	  public String toString() {
	    return type + " " + lexeme + " " + literal;
	  }
}
