package edu.cornell.cs4321.PhysicalOperators;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import edu.cornell.cs4321.Database.Tuple;
import edu.cornell.cs4321.Database.TupleComparator;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.OrderByElement;

/**
 * SortOperator to support ORDER BY clause
 * Algorithm: get all child tuples together, save them as a Tuple List, then sort them by specified attributes.
 * Only sort in ascending order as assumed.
 * @author Chenxi Su cs2238, Hao Qian hq43, Jiangjie Man jm2559
 */
public class SortOperator extends Operator {
	
	private List<Tuple> sortedTupleList = new LinkedList<>();
	private Operator childOperator;
	private List<Column> sortByColumns;
	private boolean isSorted = false;
	
	/**
	 * Construct a sort operator, will sort by columns specified.
	 * @param childOperator child operator in a query plan(tree)
	 * @param orderByList attributes to sort by
	 */
	public SortOperator(Operator childOperator, List<OrderByElement> orderByList){
		this.childOperator = childOperator;
		sortByColumns = new LinkedList<>();
		if (orderByList != null) {
			for (OrderByElement e : orderByList) {
				Column c = (Column) e.getExpression();
				sortByColumns.add(c);
			}
		}
	}

	/**
	 * After sorting all tuples retrieved from child operator, return them by sequence.
	 * @return the first tuple qualified, return null if there's no tuple qualified.
	 */
	@Override
	public Tuple getNextTuple() {
		Tuple t;
		while((t = childOperator.getNextTuple()) != null) {
			sortedTupleList.add(t);
		}
		if(!isSorted) {
			Collections.sort(sortedTupleList, new TupleComparator(sortByColumns));
			isSorted = true;
		}
		return sortedTupleList.size() > 0 ? sortedTupleList.remove(0) : null;
	}

	/**
	 * Reset method for SortOperator class.
	 */
	@Override
	public void reset() {
		this.sortedTupleList = new LinkedList<>();
		this.isSorted = false;
		childOperator.reset();
	}

	@Override
	public void reset(int index) {
		// TODO Auto-generated method stub
		
	}

}
