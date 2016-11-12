package edu.cornell.cs4321.BPlusTree;

import java.util.ArrayList;

public class indexNode extends Node{
	private ArrayList<Integer> key;
	private ArrayList<leafNode> children;
	private ArrayList<indexNode> indexChildren;
	private boolean isIndex;
	
	/**
	 * Constructor for building the first layer of indexes
	 * @param key list of the node (d<=key size<=2d)
	 * @param indexChildren: list of leafNode
	 */
	public indexNode(ArrayList<Integer> key, ArrayList<leafNode> leafChildren){
		this.key = key;
		this.children = leafChildren;
		isIndex = false;
	}
	
	/**
	 * Constructor for building the upper layers of indexes
	 * @param key list of the node (d<=key size<=2d)
	 * @param indexChildren
	 * @param isIndex
	 */
	public indexNode(ArrayList<Integer> key, ArrayList<indexNode> indexChildren, boolean isIndex){
		this.key = key;
		this.indexChildren = indexChildren;
		this.isIndex = isIndex;
	}
	
	public ArrayList<leafNode> getChildren(){
		return children;
	}
	
	public ArrayList<indexNode> getIndexChildren(){
		return indexChildren;
	}
	
	public boolean isUpperLayer(){
		return isIndex;
	}
	
	public ArrayList<Integer> getKeys(){
		return key;
	}
}
