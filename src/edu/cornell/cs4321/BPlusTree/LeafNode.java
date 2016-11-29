package edu.cornell.cs4321.BPlusTree;

import java.util.List;
import java.util.TreeMap;

/**
 * This class implements Node interface, which is used to build the leaf layer of B+ tree.
 *
 */
public class LeafNode implements Node{
	private TreeMap<Integer,List<DataEntry>> valueMap;
	private int address;
	
	public LeafNode(TreeMap<Integer,List<DataEntry>> valueMap, int address) {
		this.valueMap = valueMap;
		this.address = address;
	}

	/**
	 * Return the leaf node in TreeMap structure. Its key is a integer and its value is a list of data entries.
	 * @return leaf node
     */
	public TreeMap<Integer,List<DataEntry>> getMap(){
		return valueMap;
	}

	/**
	 * Return the number of data entries.
	 * @return number of data entries
     */
	public int numDataEntry() {
		return valueMap.size();
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
