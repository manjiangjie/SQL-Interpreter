package edu.cornell.cs4321.BPlusTree;

public class DataEntry {
	private int pageId;
	private int tupleId;
	
	public DataEntry(int pageId, int tupleId){
		this.pageId = pageId;
		this.tupleId = tupleId;
	}
	
	public int getPageId(){
		return pageId;
	}
	
	public int getTupleId(){
		return tupleId;
	}
}
