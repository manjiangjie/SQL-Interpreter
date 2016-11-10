package edu.cornell.cs4321.PhysicalOperators;

import edu.cornell.cs4321.Database.Tuple;
import edu.cornell.cs4321.Visitors.WhereExpressionVisitor;
import net.sf.jsqlparser.expression.Expression;

/**
 * Selection Operator for the use of WHERE clause.
 * This operator only handles single table, for joined table please use Join Operator.
 * @author Chenxi Su cs2238, Hao Qian hq43, Jiangjie Man jm2559
 */

public class SelectionOperator extends Operator {
	
	private Expression selectionCondition;//the root of the selection condition for this table
	private Operator childOperator;//Note: it would be the best to just give it a type as Operator instead of a specific one

	/**
	 * Constructor
	 * @param childOperator child operator in a query plan(tree)
	 * @param expr A Expression which represents of selection condition of SelectionOperator
	 * */
	public SelectionOperator(Operator childOperator, Expression expr) {
		this.selectionCondition = expr;
		this.childOperator = childOperator;
	}

	/**
	 * keep calling getNextTuple until one validates all the condition has appeared, or null has been reached
	 * @return next qualified tuple
	 */
	@Override
	public Tuple getNextTuple() {
		Tuple tempTuple = childOperator.getNextTuple();
		while(tempTuple != null) {
			if (validTuple(tempTuple, selectionCondition)) {
				break;
			}
			tempTuple = childOperator.getNextTuple();
		}
		return tempTuple;
	}

	@Override
	public void reset() {
		childOperator.reset();
	}

	/**
	 * Decide whether current tuple satisfy all the condition or not
	 * Using WhereExpressionVisitor to traverse the expression tree then get result from the visitor.
	 * @param t tuple contains data record
	 * @param e valid the tuple(t) by this expression
	 * @return if valid or not
	 */
	private boolean validTuple(Tuple t, Expression e){
		if (e == null) {
			return true;
		}
		
		WhereExpressionVisitor visitor = new WhereExpressionVisitor(t);
		e.accept(visitor);

		return visitor.getResult();
	}

	@Override
	public void reset(int index) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getIndex() {
		// TODO Auto-generated method stub
		return 0;
	}

}
