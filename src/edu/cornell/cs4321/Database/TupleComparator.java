package edu.cornell.cs4321.Database;

import java.util.Comparator;
import java.util.List;

import net.sf.jsqlparser.schema.Column;

/**
 * Tuple Comparator used for sorting tuples by specified attributes
 * 
 * @author Chenxi Su cs2238, Hao Qian hq43, Jiangjie Man jm2559
 *
 */

public class TupleComparator implements Comparator<Object> {
	
	private List<Column> sortByColumns;
	
	/**
	 * Construct a comparator with attributes you want to sort by
	 * @param sortByColumns the attributes you want to sort by, will sort tuples according to the sequence of columns.
	 */
	public TupleComparator(List<Column> sortByColumns){
		this.sortByColumns = sortByColumns;
	}

	@Override
	public int compare(Object t1, Object t2) {
		if(t1 != null && t2 != null){
			Integer r1 =null ,r2 = null;
			for(Column c : sortByColumns){
				r1 = ((Tuple)t1).getValueByCol(c);
				r2 = ((Tuple)t2).getValueByCol(c);
				if(r1!=r2) return r1-r2;
			}
			for(Column e : ((Tuple)t1).getSchema()){
				r1 = ((Tuple)t1).getValueByCol(e);
				r2 = ((Tuple)t2).getValueByCol(e);
				if(r1 != r2) return r1-r2;
			}			
		}
		return 0;
	}
	

}
