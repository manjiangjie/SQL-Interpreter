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
	private ArrayList<ArrayList<Tuple>> buffers;
	private Tuple lastRightTuple;
	private int bufferIndex;
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
		buffers = new ArrayList<ArrayList<Tuple>>();
		bufferIndex = 0;
		tupleIndex = 0;
		
		Tuple t = leftChild.getNextTuple();
		ArrayList<Tuple> singleBuffer = new ArrayList<Tuple>();
		
		if(t!=null){
			//keep adding tuples to a buffer until the it is full
			//then add the full buffer to the buffer list...
			int pageMaxSize = 1024 / t.getValues().size();
			int bufferMaxSize = pageMaxSize * nBufferPages;
			while(t!=null){
				singleBuffer.add(t);
				t = leftChild.getNextTuple();
				if (singleBuffer.size()==bufferMaxSize){
					buffers.add(singleBuffer);
					singleBuffer = new ArrayList<Tuple>();
				}
			}
			if(singleBuffer.size()<bufferMaxSize){
				buffers.add(singleBuffer);
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
		//loop over every buffer in the buffer list
		for (int i = bufferIndex; i < buffers.size(); i ++){
			ArrayList<Tuple> buffer = buffers.get(i);
			bufferIndex = i;
			Tuple rightTuple;
			if(lastRightTuple == null){
				rightTuple = rightChildOperator.getNextTuple();
			}else{
				rightTuple = lastRightTuple;
			}
			//loop over every tuple in the right relation
			while(rightTuple != null){
				
				//loop over every tuple in the buffer
				for(int j = tupleIndex; j < buffer.size();j++){
					tupleIndex = j+1;
					Tuple leftTuple = buffer.get(j);
					
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
			}
			rightChildOperator.reset();
		}
		bufferIndex = 0;
		return null;	
	}

	@Override
	public void reset() {
		leftChildOperator.reset();
		rightChildOperator.reset();
	}

}
