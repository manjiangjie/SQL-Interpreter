package edu.cornell.cs4321.PhysicalOperators;

import edu.cornell.cs4321.Database.*;


/**
 * Abstract Operator class, child operators must implement getNextTuple() and reset().
 * @author Chenxi Su cs2238, Hao Qian hq43, Jiangjie Man jm2559
 *
 */

public abstract class Operator {
	/**
	 * Retrieve the next tuple qualified by the operator.
	 * @return the next qualified tuple
	 */
	public abstract Tuple getNextTuple();
	
	/**
	 * Reset state, start generating output again from the beginning.
	 */
	public abstract void reset();
	
	/**
	 * For debugging use.
	 * Print all tuples retrieved to Console.
	 */
	public void dump() {
		Tuple t;
		while((t = getNextTuple())!=null){
			System.out.println(t);
		}
	}
	
	/**
	 * Reset state to a specified index, only need to be implemented in sort operators.
	 */
	public abstract void reset(int index);
	/**
	 * Get current tuple index, only need to be implemented in sort operators.
	 */
	public abstract int getIndex();
}

