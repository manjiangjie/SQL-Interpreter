package edu.cornell.cs4321.PhysicalOperators;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import edu.cornell.cs4321.Database.Tuple;
import edu.cornell.cs4321.Database.TupleComparator;
import net.sf.jsqlparser.schema.Column;

/**
 * SortOperator to support ORDER BY clause
 * Algorithm: get all child tuples together, save them as a Tuple List, then sort them by specified attributes.
 * Only sort in ascending order as assumed.
 * @author Chenxi Su cs2238, Hao Qian hq43, Jiangjie Man jm2559
 */
public class SortOperator extends Operator {
	
	private List<Tuple> sortedTupleList = new ArrayList<>();
	private Operator childOperator;
	private List<Column> sortByColumns;
	private boolean isSorted = false;
	private int index = 0;
	
	/**
	 * Construct a sort operator, will sort by columns specified.
	 * @param childOperator child operator in a query plan(tree)
	 * @param orderByList attributes to sort by
	 */
	public SortOperator(Operator childOperator, List<Column> orderByList){
		this.childOperator = childOperator;
		sortByColumns = new LinkedList<>();
		if (orderByList != null) {
			this.sortByColumns = orderByList;
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
		this.index++;
		return sortedTupleList.size() >= this.index ? sortedTupleList.get(this.index - 1) : null;
	}

	/**
	 * Reset method for SortOperator class.
	 */
	@Override
	public void reset() {
		this.index = 0;
		this.sortedTupleList = new ArrayList<>();
		this.isSorted = false;
		childOperator.reset();
	}

	@Override
	public void reset(int index) {
		this.index = index;
	}
	
	/**
	 * Return current tuple index
	 */
	@Override
	public int getIndex() {
		return this.index;
	}

}
