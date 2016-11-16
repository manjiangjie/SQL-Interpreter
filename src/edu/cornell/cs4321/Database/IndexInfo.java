package edu.cornell.cs4321.Database;

import net.sf.jsqlparser.schema.Column;

/**
 * Data structure to store index information
 *
 * @author Hao Qian hq43
 */
public class IndexInfo {
	private Column column;
	private boolean clustered;
	private int order;
	private String indexPath;
	
	public IndexInfo(Column column, boolean clustered, int order, String indexPath) {
		this.column = column;
		this.clustered = clustered;
		this.order = order;
		this.indexPath = indexPath;
	}

	public Column getColumn() {
		return column;
	}

	public boolean isClustered() {
		return clustered;
	}

	public int getOrder() {
		return order;
	}	
	
	public String getIndexPath() {
		return this.indexPath;
	}
}
