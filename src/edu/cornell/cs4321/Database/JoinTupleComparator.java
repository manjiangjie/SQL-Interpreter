package edu.cornell.cs4321.Database;

import java.util.Comparator;
import java.util.List;

import net.sf.jsqlparser.schema.Column;

/**
 * This class can compare two tuples from two different relations on the join attributes.
 * 
 * @author Hao Qian hq43
 *
 */

public class JoinTupleComparator implements Comparator<Object> {
	
	private List<Column> joinAttrLeft;
	private List<Column> joinAttrRight;
	
	/**
	 * Construct a comparator with attributes you want to sort by
	 * @param sortByColumns the attributes you want to sort by, will sort tuples according to the sequence of columns.
	 */
	public JoinTupleComparator(List<Column> joinAttributesLeft,List<Column> joinAttributesRight){
		joinAttrLeft = joinAttributesLeft;
		joinAttrRight = joinAttributesRight;
	}

	@Override
	public int compare(Object t1, Object t2) {
		Tuple lt = (Tuple)t1;
		Tuple rt = (Tuple)t2;

		if(t1==null||t2==null){
			System.out.println("xx");
		}
		
		for(int i = 0; i < joinAttrLeft.size(); i++) {
			Column lc = joinAttrLeft.get(i);
			Column rc = joinAttrRight.get(i);
			int r1 = lt.getValueByCol(lc).intValue();
			int r2 = rt.getValueByCol(rc).intValue();
			if(r1 != r2) {
				return r1 - r2;
			}
		}
		return 0;
	}

}
