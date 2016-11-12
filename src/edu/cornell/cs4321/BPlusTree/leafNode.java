package edu.cornell.cs4321.BPlusTree;

import java.util.List;
import java.util.TreeMap;

public class leafNode extends Node{
	private TreeMap<Integer,List<dataEntry>> valueMap;
	
	public leafNode(TreeMap<Integer,List<dataEntry>> valueMap){
		this.valueMap = valueMap;
	}
	
	public TreeMap<Integer,List<dataEntry>> getMap(){
		return valueMap;
	}
}
