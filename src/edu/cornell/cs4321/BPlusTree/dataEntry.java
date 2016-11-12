package edu.cornell.cs4321.BPlusTree;

public class dataEntry {
	private int pageid; 
	private int tupleid;
	
	public dataEntry(int pageid, int tupleid){
		this.pageid = pageid;
		this.tupleid = tupleid;
	}
	
	public int getPageId(){
		return pageid;
	}
	
	public int getTupleId(){
		return tupleid;
	}
}
