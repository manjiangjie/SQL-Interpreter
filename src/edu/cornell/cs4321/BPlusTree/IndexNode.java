package edu.cornell.cs4321.BPlusTree;

import java.util.ArrayList;

public class IndexNode implements Node{
	private ArrayList<Integer> key;
	private ArrayList<LeafNode> children;
	private ArrayList<IndexNode> indexChildren;
	private boolean isIndex;
	private int address;
	private int leafKey;
	
	/**
	 * Constructor for building the first layer of indexes
	 * @param key list of the node (d<=key size<=2d)
	 * @param leafChildren: list of LeafNode
	 */
	public IndexNode(ArrayList<Integer> key, ArrayList<LeafNode> leafChildren, int address, int leafKey) {
		this.key = key;
		this.children = leafChildren;
		isIndex = false;
		this.address = address;
		this.leafKey = leafKey;
	}
	
	/**
	 * Constructor for building the upper layers of indexes
	 * @param key list of the node (d<=key size<=2d)
	 * @param indexChildren
	 * @param isIndex
	 */
	public IndexNode(ArrayList<Integer> key, ArrayList<IndexNode> indexChildren, boolean isIndex, int address, int leafKey) {
		this.key = key;
		this.indexChildren = indexChildren;
		this.isIndex = isIndex;
		this.address = address;
		this.leafKey = leafKey;
	}

	public int getLeafKey(){
		return leafKey;
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

	/**
	 * Return the index of the node's page in the index file
	 * @return address of the node
	 */
	@Override
	public int getAddress() {
		return address;
	}
}
