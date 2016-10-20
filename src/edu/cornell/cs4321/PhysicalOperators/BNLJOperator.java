package edu.cornell.cs4321.PhysicalOperators;

import edu.cornell.cs4321.Database.Tuple;
import net.sf.jsqlparser.expression.Expression;

/**
 * Block Nested Loop Join operator to retrieve a merged tuple
 * 
 * @author Heng Kuang hk856
 *
 */

public class BNLJOperator extends Operator {

	private Operator leftChildOperator;
	private Operator rightChildOperator;
	private Expression joinExpression;
	private int bufferPage;
	private boolean rightTableDone;
	private Tuple lastLeftTuple;

	public BNLJOperator(Operator leftChild, Operator rightChild, Expression joinExpression, int bufferPage) {
		leftChildOperator = leftChild;
		rightChildOperator = rightChild;
		this.joinExpression = joinExpression;
		this.bufferPage = bufferPage;
	}

	@Override
	public Tuple getNextTuple() {
		
		return null;
	}

	@Override
	public void reset() {
		leftChildOperator.reset();
		rightChildOperator.reset();
	}

}
