package edu.cornell.cs4321.BPlusTree;

import java.util.List;
import java.util.TreeMap;

public class LeafNode extends Node{
	private TreeMap<Integer,List<DataEntry>> valueMap;
	private int address;
	
	public LeafNode(TreeMap<Integer,List<DataEntry>> valueMap, int address) {
		this.valueMap = valueMap;
		this.address = address;
	}
	
	public TreeMap<Integer,List<DataEntry>> getMap(){
		return valueMap;
	}

	public int numDataEntry() {
		return valueMap.size();
	}

	public int getAddress() {
		return address;
	}

}
