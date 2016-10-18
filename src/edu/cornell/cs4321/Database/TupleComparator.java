package edu.cornell.cs4321.Database;

import java.util.Comparator;
import java.util.LinkedList;
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
		List<Column> columns = new LinkedList<>(((Tuple) t1).getSchema());

		if(t1 != null && t2 != null) {
			int r1, r2;
			for(Column c : sortByColumns) {
				for (Column column : ((Tuple) t1).getSchema()) {
					if (column.getColumnName().equals(c.getColumnName())) {
						columns.remove(column);
					}
				}
				r1 = ((Tuple) t1).getValueByCol(c).intValue();
				r2 = ((Tuple) t2).getValueByCol(c).intValue();
				if(r1 != r2) {
					return r1 - r2;
				}
			}
			//System.out.println(columns.toString());
			for(Column e : columns) {
				r1 = ((Tuple) t1).getValueByCol(e).intValue();
				r2 = ((Tuple) t2).getValueByCol(e).intValue();
				if(r1 != r2) {
					return r1 - r2;
				}
			}
		}
		return 0;
	}

}
