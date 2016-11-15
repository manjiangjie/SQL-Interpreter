package edu.cornell.cs4321.BPlusTree;

import java.util.List;
import java.util.TreeMap;

public class leafNode extends Node{
	private TreeMap<Integer,List<DataEntry>> valueMap;
	
	public leafNode(TreeMap<Integer,List<DataEntry>> valueMap){
		this.valueMap = valueMap;
	}
	
	public TreeMap<Integer,List<DataEntry>> getMap(){
		return valueMap;
	}
}
