package com.zhennan.doo;

import java.util.List;
import java.util.Map;

import com.zhennan.doo.Expr.Variable;
import java.util.ArrayList;
import java.util.HashMap;

class Interpreter implements Expr.Visitor<Object>, Stmt.Visitor<Void> {
	//new globals field holds a fixed reference to the outermost global environment.
	final Environment globals = new Environment();
	private Environment environment = globals;
	
	Interpreter() {
	    globals.define("clock", new Callable() {
	      @Override
	      public int arity() { return 0; }

	      @Override
	      public Object call(Interpreter interpreter,
	                         List<Object> arguments) {
	        return (double)System.currentTimeMillis() / 1000.0;
	      }

	      @Override
	      public String toString() { return "<native fn>"; }
	    });
	  }
	// interpret ���
	void interpret(List<Stmt> statements) {
		try {
			for (Stmt statement : statements) {
				execute(statement);
			}
		} catch (RuntimeError error) {
			Do.runtimeError(error);
		}
	}

	private void execute(Stmt stmt) {
		stmt.accept(this);
	}

	// let an expression to be evaluated by this interpreter.
	private Object evaluate(Expr expr) {
		return expr.accept(this);
	}
	@Override
	  public Void visitClassStmt(Stmt.Class stmt) {
	    environment.define(stmt.name.lexeme, null);
	    Map<String, Function> methods = new HashMap<>();
	    for (Stmt.Function method : stmt.methods) {
	      Function function = new Function(method, environment);
	      methods.put(method.name.lexeme, function);
	    }

	    DoClass klass = new DoClass(stmt.name.lexeme, methods);
	    environment.assign(stmt.name, klass);
	    
	    //executeBlock(stmt.methods, new Environment(environment));
		return null;
	  }
	
	@Override
	  public Void visitFunctionStmt(Stmt.Function stmt) {
		//nested closure environment
	    Function function = new Function(stmt, environment);
	    environment.define(stmt.name.lexeme, function);
	    return null;
	  }
	@Override
	public Void visitWhileStmt(Stmt.While stmt) {
		while (isTruthy(evaluate(stmt.condition))) {
			execute(stmt.body);
		}
		return null;
	}
	@Override
	public Void visitIfStmt(Stmt.If stmt) {
		if (isTruthy(evaluate(stmt.condition))) {
			execute(stmt.thenBranch);
		} else if (stmt.elseBranch != null) {
			execute(stmt.elseBranch);
		}
		return null;
	}

	@Override
	public Void visitBlockStmt(Stmt.Block stmt) {
		executeBlock(stmt.statements, new Environment(environment));
		return null;
	}

	void executeBlock(List<Stmt> statements, Environment environment) {
		Environment previous = this.environment;
		try {
			//����ʵ����environment�Ĵ����� �������
			this.environment = environment;
			//�ұߵ�environment����block����ģ���ʱ����������enclosing

			for (Stmt statement : statements) {
				execute(statement);
			}
		} finally {
			//blockִ���꣬ environment�˻س�ʼ
			this.environment = previous;
		}
	}

	// ����expression����û��return type
	@Override
	public Void visitExpressionStmt(Stmt.Expression stmt) {
		Object value = evaluate(stmt.expression);
		System.out.println(""+stringify(value));
		return null;
	}

	@Override
	public Void visitPrintStmt(Stmt.Print stmt) {
		Object value = evaluate(stmt.expression);
		System.out.println(stringify(value));
		return null;
	}

	@Override
	public Void visitVarStmt(Stmt.Var stmt) {
		Object value = null;
		if (stmt.initializer != null) {
			value = evaluate(stmt.initializer);
		}

		environment.define(stmt.name.lexeme, value);

		// no stringify because assignment doesnt require print anything
		return null;
	}
	  @Override
	  public Void visitReturnStmt(Stmt.Return stmt) {
	    Object value = null;
	    if (stmt.value != null) value = evaluate(stmt.value);

	    throw new Return(value);
	  }
	  
	  

	private String stringify(Object object) {
		if (object == null)
			return "nil";

		if (object instanceof Double) {
			String text = object.toString();
			if (text.endsWith(".0")) {
				text = text.substring(0, text.length() - 2);
			}
			return text;
		}

		return object.toString();
	}
	@Override
	  public Object visitGetExpr(Expr.Get expr) {
	    Object object = evaluate(expr.object);
	    if (object instanceof Instance) {
	      return ((Instance) object).get(expr.name);
	    }

	    throw new RuntimeError(expr.name,
	        "Only instances have properties.");
	  }
	@Override
	  public Object visitSetExpr(Expr.Set expr) {
	    Object object = evaluate(expr.object);

	    if (!(object instanceof Instance)) { 
	      throw new RuntimeError(expr.name,
	                             "Only instances have fields.");
	    }

	    Object value = evaluate(expr.value);
	    ((Instance)object).set(expr.name, value);
	    return value;
	  }
	@Override
	public Object visitCallExpr(Expr.Call expr) {
		
		//get the function by identifier
		Object callee = evaluate(expr.callee);

		List<Object> arguments = new ArrayList<>();
		for (Expr argument : expr.arguments) { 
			arguments.add(evaluate(argument));
		}
		//if not callable function, output error message
		if (!(callee instanceof Callable)) {
			throw new RuntimeError(expr.paren,
					"Can only call functions and classes.");
		}
		Callable function = (Callable)callee;
		if (arguments.size() != function.arity()) {
			throw new RuntimeError(expr.paren, "Expected " +
					function.arity() + " arguments but your input has " +
					arguments.size() + ".");
		}
		return function.call(this, arguments);
	}

	@Override
	public Object visitLogicalExpr(Expr.Logical expr) {
		Object left = evaluate(expr.left);

		if (expr.operator.type == TokenType.OR) {
			if (isTruthy(left)) return left;
		} else {
			if (!isTruthy(left)) return left;
		}

		//left is known to be true here, now check right part is truthy or not
		return evaluate(expr.right);
	}
	// visit a assign expression
	@Override
	public Object visitAssignExpr(Expr.Assign expr) {
		Object value = evaluate(expr.value);
		//�ȳ���assign����ǰscope�� ����Ҳ���key���������ϡ�������Ҳ���������runtimeerror
		environment.assign(expr.name, value);
		return value;
	}

	// visit a variable / identifier

	@Override
	public Object visitVariableExpr(Expr.Variable expr) {
		//�ӵ�ǰscope��ʼ��keyһֱ���ϣ�����Ҳ����򱨴�
		return environment.get(expr.name);
	}

	@Override
	public Object visitLiteralExpr(Expr.Literal expr) {
		return expr.value;
	}

	@Override
	public Object visitGroupingExpr(Expr.Grouping expr) {
		return evaluate(expr.expression);
	}

	@Override
	public Object visitBinaryExpr(Expr.Binary expr) {
		Object left = evaluate(expr.left);
		Object right = evaluate(expr.right);

		// true + trur?? whats gonna happen
		switch (expr.operator.type) {
		case MINUS:
			// check runtime error
			checkNumberOperands(expr.operator, left, right);
			return (double) left - (double) right;
		case SLASH:
			checkNumberOperands(expr.operator, left, right);
			return (double) left / (double) right;
		case STAR:
			checkNumberOperands(expr.operator, left, right);
			return (double) left * (double) right;
		case PLUS:

			if (left instanceof Double && right instanceof Double) {
				return (double) left + (double) right;
			}

			if (left instanceof String && right instanceof String) {
				return (String) left + (String) right;
			}
			throw new RuntimeError(expr.operator, "Operands must be two numbers or two strings.");

		case GREATER:
			checkNumberOperands(expr.operator, left, right);
			return (double) left > (double) right;
		case GREATER_EQUAL:
			checkNumberOperands(expr.operator, left, right);
			return (double) left >= (double) right;
		case LESS:
			checkNumberOperands(expr.operator, left, right);
			return (double) left < (double) right;
		case LESS_EQUAL:
			checkNumberOperands(expr.operator, left, right);
			return (double) left <= (double) right;
		case NOT_EQUAL:
			checkNumberOperands(expr.operator, left, right);
			return !isEqual(left, right);
		case EQUAL_EQUAL:
			checkNumberOperands(expr.operator, left, right);
			return isEqual(left, right);
		}

		// Unreachable.
		return null;
	}

	private void checkNumberOperands(Token operator, Object left, Object right) {
		if (left instanceof Double && right instanceof Double)
			return;

		throw new RuntimeError(operator, "Operands must be numbers.");
	}

	private void checkNumberOperand(Token operator, Object operand) {
		if (operand instanceof Double)
			return;
		throw new RuntimeError(operator, "Operand must be a number.");
	}

	private boolean isEqual(Object a, Object b) {
		if (a == null && b == null)
			return true;
		if (a == null)
			return false;

		// a and b can only be boolean, double, and string, so we can use
		// Java's equals method directly
		return a.equals(b);
	}

	@Override
	public Object visitUnaryExpr(Expr.Unary expr) {
		Object right = evaluate(expr.right);

		switch (expr.operator.type) {
		case NOT:
			return !isTruthy(right);
		case MINUS:
			checkNumberOperand(expr.operator, right);
			return -(double) right;
		}

		// Unreachable.
		return null;
	}

	private boolean isTruthy(Object object) {
		// if null, return false
		if (object == null)
			return false;
		// if boolean, return what it is
		if (object instanceof Boolean)
			return (boolean) object;
		// anything else is "truthy". Meaning: treated as true
		return true;
	}

}
