package com.zhennan.doo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Scanner;

/**
 * @author supremedad
 *
 */
public class Do {
	static boolean hadError = false;
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
	}

	private static void runPrompt() throws IOException {
		Scanner input = new Scanner(System.in);

		for (;;) {
			System.out.print("> ");
			String line = input.nextLine();
			if (line == null)
				break;
			run(line);
			hadError = false;
		}
	}

	private static void run(String source) {
		Lexer scanner = new Lexer(source);
		List<Token> tokens = scanner.scanTokens();

		// For now, just print the tokens.
		for (Token token : tokens) {
			System.out.println(token);
		}
	}

	static void error(int line, String message) {
		report(line, "", message);
	}

	private static void report(int line, String where, String message) {
		System.err.println("[line " + line + "] Error" + where + ": " + message);
		hadError = true;
	}

}
