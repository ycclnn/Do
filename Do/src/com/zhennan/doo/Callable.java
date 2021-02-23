package com.zhennan.doo;
import java.util.List;

interface Callable {
	int arity();
	Object call(Interpreter interpreter, List<Object> arguments);
}
