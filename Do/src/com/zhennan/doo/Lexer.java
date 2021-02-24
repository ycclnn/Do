package com.zhennan.doo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static com.zhennan.doo.TokenType.*;

class Lexer {
	 private static final Map<String, TokenType> keywords;
	 //to check whether the token is one of the reserved word below.
	  static {
	    keywords = new HashMap<>();
	    keywords.put("and",    AND);
	    keywords.put("class",  CLASS);
	    keywords.put("else",   ELSE);
	    keywords.put("false",  FALSE);
	    keywords.put("fun",    FUN);
	    keywords.put("if",     IF);
	    keywords.put("nil",    NIL);
	    keywords.put("or",     OR);
	    keywords.put("print",  PRINT);
	    keywords.put("return", RETURN);
	    keywords.put("true",   TRUE);
	    keywords.put("var",    VAR);
	    keywords.put("while",  WHILE);
	  }
	private final String source;
	private final List<Token> tokens = new ArrayList<>();
	// start char
	private int start = 0;
	// current char
	private int current = 0;
	// line number
	private int line = 1;

	Lexer(String source) {
		this.source = source;
	}

	List<Token> scanTokens() {
		while (!isAtEnd()) {
			start = current;
			scanToken();
		}
		tokens.add(new Token(EOF, "", null, line));
		return tokens;
	}

	@SuppressWarnings("fallthrough")
	private void scanToken() {
		// 此时current index已在c后面一位
		char c = advance();
		switch (c) {
		case '(':
			addToken(LEFT_PAREN);
			break;
		case ')':
			addToken(RIGHT_PAREN);
			break;
		case '{':
			addToken(LEFT_BRACE);
			break;
		case '}':
			addToken(RIGHT_BRACE);
			break;
		case ',':
			addToken(COMMA);
			break;
		case '.':
			addToken(DOT);
			break;
		case '-':
			addToken(MINUS);
			break;
		case '+':
			addToken(PLUS);
			break;
		case ';':
			addToken(SEMICOLON);
			break;
		case '*':
			addToken(STAR);
			break;
		case '!':
			addToken(match('=') ? NOT_EQUAL : NOT);
			break;
		case '=':
			addToken(match('=') ? EQUAL_EQUAL : EQUAL);
			break;
		case '<':
			addToken(match('=') ? LESS_EQUAL : LESS);
			break;
		case '>':
			addToken(match('=') ? GREATER_EQUAL : GREATER);
			break;
		case '/':
			if (match('/')) {
				// A comment goes until the end of the line.
				while (peek() != '\n' && !isAtEnd())
					advance();
			} else {
				addToken(SLASH);
			}
		case ' ':
		case '\r':
		case '\t':
			// Ignore whitespace.
			break;

		case '\n':
			line++;
			break;
		case '"':
			string();
			break;
		
		default:
			if (isDigit(c)) {
				number();
			} else if (isAlpha(c)) {
				identifier();
			} else {
				Do.error(line, "Unexpected character!");
			}
			break;
		}
	}
	//check whether the token is identifier or not. "or" and "orchid" 
	//example always detect as orchid. 
	private void identifier() {
	    while (isAlphaNumeric(peek())) advance();
	    //if the type is one of the reserved word, then change its type
	    String text = source.substring(start, current);
	    TokenType type = keywords.get(text);
	    if (type == null) type = IDENTIFIER;
	    addToken(type);
	    
	  }
	private boolean isAlpha(char c) {
	    return (c >= 'a' && c <= 'z') ||
	           (c >= 'A' && c <= 'Z') ||
	            c == '_';
	  }

	  private boolean isAlphaNumeric(char c) {
	    return isAlpha(c) || isDigit(c);
	  }
	// check if the char is a digit
	private boolean isDigit(char c) {
		return c >= '0' && c <= '9';
	}

	// comsume digit to be a number, add it to the token list
	private void number() {
		while (isDigit(peek()))
			advance();

		// Look for a fractional part.
		if (peek() == '.' && isDigit(peekNext())) {
			// Consume the "."
			advance();

			while (isDigit(peek()))
				advance();
		}

		addToken(NUMBER, Double.parseDouble(source.substring(start, current)));
	}

	// check the next val without popoing it
	private char peekNext() {
		if (current + 1 >= source.length())
			return '\0';
		return source.charAt(current + 1);
	}

	private void string() {
		while (peek() != '"' && !isAtEnd()) {
			if (peek() == '\n') {
				line++;
			}
			advance();
		}

		if (isAtEnd()) {
			Do.error(line, "Unterminated string.");
			return;
		}

		// code goes here meaning it finds a closing "
		advance();

		// Trim the "". only use the pure string literal
		String value = source.substring(start + 1, current - 1);
		addToken(STRING, value);
	}

	// check current val, without remove it
	private char peek() {
		if (isAtEnd())
			return '\0';
		return source.charAt(current);
	}

	// check if current
	private boolean match(char expected) {
		if (isAtEnd())
			return false;
		if (source.charAt(current) != expected)
			return false;

		current++;
		return true;
	}

	// helper method section
	private boolean isAtEnd() {
		return current >= source.length();
	}

	// current next char, current++
	private char advance() {
		current++;
		return source.charAt(current - 1);
	}

	//
	private void addToken(TokenType type) {
		addToken(type, null);
	}

	// add token to the token list
	private void addToken(TokenType type, Object literal) {
		//get the token
		String text = source.substring(start, current);
		tokens.add(new Token(type, text, literal, line));
	}
}
