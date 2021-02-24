package com.zhennan.doo;

import java.util.HashMap;
import java.util.Map;

class Environment {
	private final Map<String, Object> values = new HashMap<>();

	//Õ‚≤øenvironment
	final Environment enclosing;

	Environment() {
		enclosing = null;
	}
	Environment(Environment enclosing) {
		this.enclosing = enclosing;
	}
	void define(String name, Object value) {
		values.put(name, value);
	}

	Object get(Token name) {
		if (values.containsKey(name.lexeme)) {
			return values.get(name.lexeme);
		}
		//first find in current environment
		//if does not exist, find in outer scope
		if (enclosing != null) return enclosing.get(name);
		throw new RuntimeError(name, "Undefined variable '" + name.lexeme + "'.");
	}

	//for assignment 
	void assign(Token name, Object value) {
		if (values.containsKey(name.lexeme)) {
			values.put(name.lexeme, value);
			return;
		}
		//try to put new value in the lowest level environment chain
		if (enclosing != null) {
			enclosing.assign(name, value);
			return;
		}
		throw new RuntimeError(name,
				"Undefined variable '" + name.lexeme + "'.");
	}
}