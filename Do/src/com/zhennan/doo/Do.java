package com.zhennan.doo;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Scanner;

/**
 * @author Zhennan
 *	Main program. READ file or REPL
 */
public class Do {
	//static error for lexer or parser. Ex.  "(" without ")"
	static boolean hadError = false;
	//dynamic error for interpreter. Ex. "a" - "b"
	 static boolean hadRuntimeError = false;
	 //static and final, because we want to track the state and value of identifiers
	 //when the interpreter stores global variables. Those variables should persist throughout the REPL session.
	 private static final Interpreter interpreter = new Interpreter();
	/**
	 * @param args
	 */
	public static void main(String[] args) throws IOException {
		if (args.length > 1) {
			System.out.println("Usage: Do [script]");
			System.exit(64);
		} else if (args.length == 1) {
			runFile(args[0]);
		} else {
			runPrompt();
		}
	}

	private static void runFile(String path) throws IOException {
		byte[] bytes = Files.readAllBytes(Paths.get(path));
		run(new String(bytes, Charset.defaultCharset()));
		
		//if read a file, exit directly
		//if read in a REPL, continue
		if (hadError) {
			System.exit(65);
		}
		//if read a file, exit directly
		//if read in a REPL, continue
		if (hadRuntimeError) {
			System.exit(70);
		}
		
	}

	private static void runPrompt() throws IOException {
	
		@SuppressWarnings("resource")
		Scanner input = new Scanner(System.in);

		for (;;) {
			System.out.print("> ");
			String line = input.nextLine();
			if (line == null)
				break;
			run(line);
			hadError = false;
		}
		input.close();
	}

	private static void run(String source) {
		Lexer scanner = new Lexer(source);
		List<Token> tokens = scanner.scanTokens();
	
		Parser parser = new Parser(tokens);
		List<Stmt> statements = parser.parse();
	    

	    // Stop if there was a syntax error.
	    if (hadError) return;
	    interpreter.interpret(statements);
	}

	static void error(int line, String message) {
		report(line, "", message);
	}

	private static void report(int line, String where, String message) {
		System.err.println("[line " + line + "] Error" + where + ": " + message);
		hadError = true;
	}
	static void error(Token token, String message) {
	    if (token.type == TokenType.EOF) {
	      report(token.line, " at end", message);
	    } else {
	      report(token.line, " at '" + token.lexeme + "'", message);
	    }
	  }
	static void runtimeError(RuntimeError error) {
	    System.err.println(error.getMessage() +
	        "\n[line " + error.token.line + "]");
	    hadRuntimeError = true;
	  }

}
