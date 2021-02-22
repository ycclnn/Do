package com.zhennan.doo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static com.zhennan.doo.TokenType.*;

class Lexer {
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

	private void scanToken() {
		//此时current index已在c后面一位
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
	          while (peek() != '\n' && !isAtEnd()) advance();
	        } else {
	          addToken(SLASH);
	        }
	        
		default:
			Do.error(line, "Unexpected character!");
			break;
		}
	}
	//check current val, without remove it
	private char peek() {
	    if (isAtEnd()) return '\0';
	    return source.charAt(current);
	  }
	//check if current 
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
		String text = source.substring(start, current);
		tokens.add(new Token(type, text, literal, line));
	}
}
