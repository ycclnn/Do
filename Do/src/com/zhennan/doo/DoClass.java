package com.zhennan.doo;

import java.util.List;
import java.util.Map;

class DoClass implements Callable {
	final String name;
	private final Map<String, Function> methods;

	DoClass(String name, Map<String, Function> methods) {
	    this.name = name;
	    this.methods = methods;
	  }

	
	@Override
	public String toString() {
		return name + "nimabi";
	}

	@Override
	public Object call(Interpreter interpreter, List<Object> arguments) {
		Instance instance = new Instance(this);
		return instance;
	}
	Function findMethod(String name) {
	    if (methods.containsKey(name)) {
	      return methods.get(name);
	    }

	    return null;
	  }
	@Override
	public int arity() {
		return 0;
	}
}
