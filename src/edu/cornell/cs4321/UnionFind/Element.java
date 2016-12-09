package edu.cornell.cs4321.UnionFind;

import java.util.HashSet;

import net.sf.jsqlparser.expression.BinaryExpression;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.GreaterThanEquals;
import net.sf.jsqlparser.schema.Column;

public class Element {
	private HashSet<Column> attributes;
	private Long upperBound;
	private Long lowerBound;
	private Long equal;

	public Element() {
		attributes = new HashSet<Column>();
		upperBound = null;
		lowerBound = null;
		equal = null;
	}

	public HashSet<Column> getAttribute(){
		return attributes;	
	}
	
	
	/**
	 * add the input expression to output expression
	 * 
	 * @param expression
	 */
	private void addToExpression(BinaryExpression newExpression, Expression e) {
		if (newExpression.getLeftExpression() == null)
			newExpression.setLeftExpression(e);
		else if (newExpression.getRightExpression() == null)
			newExpression.setRightExpression(e);
		else
			newExpression = new AndExpression(newExpression, e);
	}

	/**
	 * build Expression based on table name
	 * @param tableName
	 * @return
	 */
	public BinaryExpression buildExpressionTree(String tableName) {
		if (!attributes.isEmpty()) {
			AndExpression expression = new AndExpression();
			for (Column c : attributes) {
				if (c.getTable().getName().equals(tableName)) {
					if (equal != null) {
						EqualsTo e = new EqualsTo();
						e.setLeftExpression(c);
						e.setRightExpression(new LongValue(equal));
						addToExpression(expression, e);
					} else {
						if (upperBound != null) {
							GreaterThanEquals gte = new GreaterThanEquals();
							gte.setLeftExpression(new LongValue(upperBound));
							gte.setRightExpression(c);
							addToExpression(expression, gte);
						}
						if (lowerBound != null) {
							GreaterThanEquals gte2 = new GreaterThanEquals();
							gte2.setLeftExpression(c);
							gte2.setRightExpression(new LongValue(lowerBound));
							addToExpression(expression, gte2);
						}
					}
				}
			}
			return expression;
		}

		return null;
	}

	public void setUpperBound(long u) {
		if(upperBound==null||(upperBound>u&&equal==null))
			upperBound = u;
	}

	public void setLowerBound(long l) {
		if(lowerBound==null||(lowerBound<l&&equal==null))
			lowerBound = l;
	}

	public void setEquality(long e) {
		equal = e;
	}

	public Long getUpperBound() {
		return upperBound;
	}

	public Long getLowerBound() {
		return lowerBound;
	}

	public Long getEquality() {
		return equal;
	}

	public void addColumn(Column col) {
		attributes.add(col);
	}

	public boolean checkColumn(Column col) {
		for(Column c : attributes) {
			if(c.getTable().getName().equals(col.getTable().getName())) {
				if(c.getColumnName().equals(col.getColumnName())){
					return true;
				}
			}
		}
		return false;
	}
}
