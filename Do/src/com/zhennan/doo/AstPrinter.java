package com.zhennan.doo;

class AstPrinter implements Expr.Visitor<String> {
	
	//top down, recursive descent parser的起始点
	String print(Expr expr) {
		return expr.accept(this);
	}

	@Override
	public String visitBinaryExpr(Expr.Binary expr) {
		System.out.println("visit binary porinter");
		return parenthesize(expr.operator.lexeme, expr.left, expr.right);
	}

	@Override
	public String visitGroupingExpr(Expr.Grouping expr) {
		System.out.println("visit group porinter");
		return parenthesize("group", expr.expression);
	}

	@Override
	public String visitLiteralExpr(Expr.Literal expr) {
		System.out.println("visit literal porinter");
		if (expr.value == null)
			return "nil";
		return expr.value.toString();
	}

	@Override
	public String visitUnaryExpr(Expr.Unary expr) {
		System.out.println("visit unary porinter");
		return parenthesize(expr.operator.lexeme, expr.right);
	}

	//simulate a syntax tree
	private String parenthesize(String name, Expr... exprs) {
		StringBuilder builder = new StringBuilder();

		builder.append("(").append(name);
		for (Expr expr : exprs) {
			builder.append(" ");
			builder.append(expr.accept(this));
		}
		builder.append(")");

		return builder.toString();
	}

	public static void main(String[] args) {
		
		Expr expression = new Expr.Binary(
				new Expr.Unary(new Token(TokenType.MINUS, "-", null, 1), new Expr.Literal(123)),
				new Token(TokenType.STAR, "*", null, 1), new Expr.Grouping(new Expr.Literal(45.67)));

		System.out.println(new AstPrinter().print(expression));
	}
}
