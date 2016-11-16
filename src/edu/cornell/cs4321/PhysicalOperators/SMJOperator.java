/**
 * 
 */
package edu.cornell.cs4321.PhysicalOperators;

import java.util.Comparator;
import edu.cornell.cs4321.Database.JoinTupleComparator;
import edu.cornell.cs4321.Database.Tuple;
import edu.cornell.cs4321.Database.TupleComparator;
import edu.cornell.cs4321.Visitors.JoinAttrExtractVisitor;
import net.sf.jsqlparser.expression.Expression;

/**
 * This class implements Sort-Merge Join Operator.
 * @author Hao Qian hq43
 *
 */
public class SMJOperator extends Operator {
	
	private Operator leftChild;
	private Operator rightChild;
	private Expression joinExpression;
	private Comparator<Object> joinComparator;
	private Comparator<Object> leftComparator;
	private Comparator<Object> rightComparator;
	private Tuple lastLeftTuple;
	private Tuple g;
	private int lastPartitionIndex;
	
	public SMJOperator(Operator leftChild, Operator rightChild, Expression joinExpression) {
		this.leftChild = leftChild;
		this.rightChild = rightChild;
		this.joinExpression = joinExpression;
		JoinAttrExtractVisitor visitor = new JoinAttrExtractVisitor();
		this.joinExpression.accept(visitor);
		this.joinComparator = new JoinTupleComparator(visitor.getLeftAttrList(), visitor.getRightAttrList());
		this.leftComparator = new JoinTupleComparator(visitor.getLeftAttrList(), visitor.getLeftAttrList());
		this.rightComparator = new JoinTupleComparator(visitor.getRightAttrList(), visitor.getRightAttrList());
		this.lastLeftTuple = null;
		this.g = null;
		this.lastPartitionIndex = 0;
	}

	/* (non-Javadoc)
	 * @see edu.cornell.cs4321.PhysicalOperators.Operator#getNextTuple()
	 */
	@Override
	public Tuple getNextTuple() {
		Tuple t = lastLeftTuple!=null ? lastLeftTuple : (lastLeftTuple = leftChild.getNextTuple());
		Tuple s = rightChild.getNextTuple();
		if(g!=null){
			if(s!=null && rightComparator.compare(s,g) == 0){
				lastLeftTuple = t;
				return new Tuple(t,s);
			} else {
				t = leftChild.getNextTuple();
				if(t==null) return null;
				if(leftComparator.compare(t, lastLeftTuple) == 0) {
					rightChild.reset(lastPartitionIndex);
					s = rightChild.getNextTuple();
					lastLeftTuple = t;
					return new Tuple(t,s);
				} else {
					lastLeftTuple = null;
				}
			}
		}
		g = s; //start of current S-partition
		lastPartitionIndex = rightChild.getIndex() - 1;
		while(t!=null && g!=null) {
			while(joinComparator.compare(t, g) < 0) {
				t = leftChild.getNextTuple();
			}
			while(joinComparator.compare(t, g) > 0) {
				g = rightChild.getNextTuple();
				lastPartitionIndex++;
			}
			s = g;
			if(joinComparator.compare(t, s) == 0) {
				lastLeftTuple = t;
				return new Tuple(t,s);
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see edu.cornell.cs4321.PhysicalOperators.Operator#reset()
	 */
	@Override
	public void reset() {
		// TODO Auto-generated method stub

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
