package com.zhennan.doo;

class Interpreter implements Expr.Visitor<Object> {

	// interpret Èë¿Ú
	void interpret(Expr expression) {
		try {
			Object value = evaluate(expression);
			
			System.out.println(stringify(value));
		} catch (RuntimeError error) {
			Do.runtimeError(error);
		}
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

	// let an expression to be evaluated by this interpreter.
	private Object evaluate(Expr expr) {
		return expr.accept(this);
	}
}
