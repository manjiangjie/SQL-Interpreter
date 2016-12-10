package edu.cornell.cs4321.PhysicalOperators;

import java.util.ArrayList;

import edu.cornell.cs4321.Database.Tuple;
import edu.cornell.cs4321.Visitors.WhereExpressionVisitor;
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
	private ArrayList<Tuple> block;
	private Tuple lastRightTuple;
	private int nPages;
	private int tupleIndex;

	/**
	 * Construct a BNLJ operator
	 * @param outer relation operator
	 * @param inner relation operator
	 * @param joinExpression
	 * @param buffer size in pages
	 */
	public BNLJOperator(Operator leftChild, Operator rightChild, Expression joinExpression, int nBufferPages) {
		
		leftChildOperator = leftChild;
		rightChildOperator = rightChild;
		this.joinExpression = joinExpression;
		nPages = nBufferPages;
		block = new ArrayList<Tuple>();
		tupleIndex = 0;
		
	}

	/**
	 * Clear the block and fill it up with tuples from left child
	 */
	private void loadTheBuffer(){
		block.clear();
		Tuple t = leftChildOperator.getNextTuple();
		
		if(t!=null){
			//keep adding tuples to a buffer until the it is full
			//then add the full buffer to the buffer list...
			int bufferMaxSize = nPages * 1024 / t.getValues().size();
			block.add(t);
			while(block.size() < bufferMaxSize ){
				t = leftChildOperator.getNextTuple();
				if(t==null) break;
				block.add(t);
			}
		}
	}
	
	/**
	 * implement block nested loop join algorithm.
	 * Scan each block of the outer child, scan inner child completely and scan all tuple in the block.
	 * @return a merged tuple of an tuple from outer child block and inner child tuple, 
	 * 			return null if there's no qualified tuple.
	 */
	@Override
	public Tuple getNextTuple() {	
		
		if(block.isEmpty()){
			loadTheBuffer();
		}
		
		//loop over every block
		while(!block.isEmpty()){
			Tuple rightTuple;
			if(lastRightTuple == null){
				rightTuple = rightChildOperator.getNextTuple();
				lastRightTuple = rightTuple;
			}else{
				rightTuple = lastRightTuple;
			}
			
			//loop over every tuple in the right relation
			while(rightTuple != null){
				
				//loop over every tuple in the buffer(left child)
				for(int j = tupleIndex; j < block.size();j++){
					tupleIndex = j+1;
					Tuple leftTuple = block.get(j);
					//return based on condition
					if(joinExpression != null){
						WhereExpressionVisitor visitor = new WhereExpressionVisitor(new Tuple(leftTuple, rightTuple));
						joinExpression.accept(visitor);
						if(visitor.getResult()){
							return visitor.getTuple();
						}
					} else {
						return new Tuple(leftTuple, rightTuple);
					}
				}
				tupleIndex = 0;
				rightTuple = rightChildOperator.getNextTuple();
				lastRightTuple = rightTuple;
			}
			loadTheBuffer();
			rightChildOperator.reset();
			lastRightTuple = null;
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
	
	public Expression getJoinExpression() {
		return this.joinExpression;
	}

	public Operator getLeftChild() {
		return this.leftChildOperator;
	}

	public Operator getRightChild() {
		return this.rightChildOperator;
	}
}
