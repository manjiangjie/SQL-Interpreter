package edu.cornell.cs4321.BPlusTree;

import java.util.ArrayList;

public class IndexNode extends Node{
	private ArrayList<Integer> key;
	private ArrayList<LeafNode> children;
	private ArrayList<IndexNode> indexChildren;
	private boolean isIndex;
	private int address;
	
	/**
	 * Constructor for building the first layer of indexes
	 * @param key list of the node (d<=key size<=2d)
	 * @param leafChildren: list of LeafNode
	 */
	public IndexNode(ArrayList<Integer> key, ArrayList<LeafNode> leafChildren, int address) {
		this.key = key;
		this.children = leafChildren;
		isIndex = false;
		this.address = address;
	}
	
	/**
	 * Constructor for building the upper layers of indexes
	 * @param key list of the node (d<=key size<=2d)
	 * @param indexChildren
	 * @param isIndex
	 */
	public IndexNode(ArrayList<Integer> key, ArrayList<IndexNode> indexChildren, boolean isIndex, int address) {
		this.key = key;
		this.indexChildren = indexChildren;
		this.isIndex = isIndex;
		this.address = address;
	}
	
	public ArrayList<LeafNode> getChildren(){
		return children;
	}
	
	public ArrayList<IndexNode> getIndexChildren(){
		return indexChildren;
	}
	
	public boolean isUpperLayer(){
		return isIndex;
	}
	
	public ArrayList<Integer> getKeys(){
		return key;
	}

	public int getAddress() {
		return address;
	}
}
