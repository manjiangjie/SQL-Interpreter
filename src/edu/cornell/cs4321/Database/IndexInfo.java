package edu.cornell.cs4321.Database;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

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
	private int numLeaves;
	private final int SIZE = 4096;
	
	public IndexInfo(Column column, boolean clustered, int order, String path) {
		this.column = column;
		this.clustered = clustered;
		this.order = order;
		this.filePath = path;
		this.indexPath = path + "indexes/" + column.getTable().getName() + "." + column.getColumnName();
		try {
			FileInputStream fin = new FileInputStream(indexPath);
            FileChannel fc = fin.getChannel();
            ByteBuffer bb = ByteBuffer.allocate(SIZE);
    		bb.clear();
    		fc.read(bb); // Read header page which stores metadata about the BPlusTree.
    		bb.flip();
    		numLeaves = bb.getInt(4);    		
    		fc.close();
    		fin.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
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
	
	public int getNumLeaves() {
		return this.numLeaves;
	}
	
	public void setAlias(String alias) {
		this.column.getTable().setName(alias);
	}
	
	public void reset(String tableName) {
		this.column.getTable().setName(tableName);
	}
}
