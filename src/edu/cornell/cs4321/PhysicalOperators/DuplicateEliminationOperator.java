package edu.cornell.cs4321.PhysicalOperators;

import edu.cornell.cs4321.Database.Tuple;

/**
 * DISTINCT Operator used to retrieve non-duplicate tuples
 * 
 * @author Chenxi Su cs2238, Hao Qian hq43, Jiangjie Man jm2559
 *
 */

public class DuplicateEliminationOperator extends Operator {
	
	private Operator childOperator;
	private Tuple lastTuple; //record the tuple retrieved previously.
	
	/**
	 * Construct a DISTINCT operator, make it the parent of childOperator in a query plan tree.
	 * @param childOperator child operator in the tree of a query plan.
	 */
	public DuplicateEliminationOperator(Operator childOperator) {
		this.childOperator = childOperator;
		this.lastTuple = null;
	}

	/**
	 * A method for retrieving next tuple
	 * @return next distinct tuple, return null if finished retrieving.
	 */
	@Override
	public Tuple getNextTuple() {
		Tuple t;
		while((t = childOperator.getNextTuple()) != null){
			if(lastTuple == null || !lastTuple.toString().equals(t.toString())){
				lastTuple = t;
				return t;
			}
		}
		return null;
	}

	/**
	 * reset method for Distinct Operator.
	 */
	@Override
	public void reset() {
		lastTuple = null;
		childOperator.reset();
	}

	@Override
	public void reset(int index) {
		// TODO Auto-generated method stub
		
	}
}
