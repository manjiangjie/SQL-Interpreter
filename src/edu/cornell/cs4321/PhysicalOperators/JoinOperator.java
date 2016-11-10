package edu.cornell.cs4321.PhysicalOperators;

import edu.cornell.cs4321.Database.Tuple;
import edu.cornell.cs4321.Visitors.WhereExpressionVisitor;
import net.sf.jsqlparser.expression.Expression;

/**
 * JOIN operator to retrieve a merged tuple
 * @author Chenxi Su cs2238, Hao Qian hq43, Jiangjie Man jm2559
 *
 */

public class JoinOperator extends Operator {
	
	private Operator leftChildOperator;
	private Operator rightChildOperator;
	private Expression joinExpression;
	private boolean rightTableDone;
	private Tuple lastLeftTuple;
	
	/**
	 * Construct a JoinOperator
	 * @param leftChild left(outer) child
	 * @param rightChild right(inner) child
	 * @param joinExpr join expression(tree), merge tuple if 
	 */
	public JoinOperator(Operator leftChild, Operator rightChild, Expression joinExpr) {
		this.leftChildOperator = leftChild;
		this.rightChildOperator = rightChild;
		this.joinExpression = joinExpr;
		this.rightTableDone = true;
		this.lastLeftTuple = null;
	}

	/**
	 * implement tuple nested loop join algorithm.
	 * Scan each tuple in the outer child once, scan inner child completely.
	 * @return a merged tuple of outer child and inner child, return null if there's no qualified tuple left.
	 */
	@Override
	public Tuple getNextTuple() {
		Tuple leftTuple;
		if(rightTableDone){
			leftTuple = leftChildOperator.getNextTuple();
			lastLeftTuple = leftTuple;
		}else{
			leftTuple = lastLeftTuple;
		}
		while(leftTuple != null ){
			Tuple rightTuple = rightChildOperator.getNextTuple();
			while(rightTuple != null){
				this.rightTableDone = false;
				if(joinExpression != null){
					WhereExpressionVisitor visitor = new WhereExpressionVisitor(new Tuple(leftTuple, rightTuple));
					joinExpression.accept(visitor);
					if(visitor.getResult()){
						return visitor.getTuple();
					}
				} else {
					return new Tuple(leftTuple, rightTuple);
				}
				rightTuple = rightChildOperator.getNextTuple();
			}
			this.rightTableDone = true;
			rightChildOperator.reset();
			leftTuple = leftChildOperator.getNextTuple();
			lastLeftTuple = leftTuple;
		}		
		return null;
	}

	@Override
	public void reset() {
		leftChildOperator.reset();
		rightChildOperator.reset();
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
