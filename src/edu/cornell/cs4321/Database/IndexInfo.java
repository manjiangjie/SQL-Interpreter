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
	private String filePath;
	
	public IndexInfo(Column column, boolean clustered, int order, String path) {
		this.column = column;
		this.clustered = clustered;
		this.order = order;
		this.filePath = path;
		this.indexPath = path + "indexes/" + column.getTable().getName() + "." + column.getColumnName();
	}

	public String getFilePath() {
		return filePath;
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
