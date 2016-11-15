package edu.cornell.cs4321.BPlusTree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;

import edu.cornell.cs4321.Database.Tuple;
import edu.cornell.cs4321.Database.TupleComparator;
import edu.cornell.cs4321.IO.BinaryTupleReader;
import edu.cornell.cs4321.IO.BinaryTupleWriter;
import net.sf.jsqlparser.schema.Column;

public class BPlusTree {
	private indexNode root;
	private ArrayList<leafNode> leafNodes;
	private int D = 2;

	/**
	 * constructor of a B+ Tree. Will build a tree once get called
	 * 
	 * @param cluster:
	 *            indicator for whether the tree is clustered or not
	 * @param tableNamn:
	 *            table used to build the tree
	 * @param column:
	 *            column of the table as index
	 */
	public BPlusTree(boolean cluster, String tableName, Column column) {
		if (cluster)
			sortCluster(tableName, column);
		leafNodes = buildLeafLayer(tableName, column);
		buildIndexLayers();
	}

	/**
	 * Sort the file if cluster is specified
	 * 
	 * @param tableName:
	 *            table to sort
	 * @param column:
	 *            sort based on indicated column
	 */
	private void sortCluster(String tableName, Column column) {
		BinaryTupleReader btr = new BinaryTupleReader(tableName);
		Tuple t = btr.readNextTuple();
		List<Tuple> tl = new LinkedList<Tuple>();
		while (t != null) {
			tl.add(t);
			t = btr.readNextTuple();
		}
		List<Column> cl = new LinkedList<Column>();
		cl.add(column);
		Collections.sort(tl, new TupleComparator(cl));
		btr.deleteFile();
		BinaryTupleWriter btw = new BinaryTupleWriter(tableName);
		for (Tuple nt : tl) {
			btw.writeNextTuple(nt);
		}
		btw.close();
	}

	/**
	 * build the very bottom layer of the tree(leaves)
	 * 
	 * @param tableName
	 * @param column
	 * @return an array list to store all leaf nodes
	 */
	private ArrayList<leafNode> buildLeafLayer(String tableName, Column column) {
		BinaryTupleReader btr = new BinaryTupleReader(tableName);
		Tuple firstTuple = btr.peek();
		int tupleSize = firstTuple.getSchema().size();
		int maxNumTuple = (4096 - 8) / (tupleSize * 4);
		int fileId = 0;
		int tupleId = 0;
		ArrayList<leafNode> leafNodes = new ArrayList<leafNode>();
		TreeMap<Integer, List<DataEntry>> wholeMap = new TreeMap<>();

		// scan all tuples and build a tree map to store all key/dataEntry pairs
		Tuple t = btr.readNextTuple();
		int key;
		while (t != null) {
			key = t.getValueByCol(column);
			if (wholeMap.containsKey(key))
				// if the node have key
				wholeMap.get(key).add(new DataEntry(fileId, tupleId));
			else {
				// map does not have the key but is not full
				ArrayList<DataEntry> tempList = new ArrayList<DataEntry>();
				tempList.add(new DataEntry(fileId, tupleId));
				wholeMap.put(key, tempList);
			}
			tupleId++;
			if (tupleId == maxNumTuple) {
				fileId++;
				tupleId = 0;
			}
		}

		// split the whole tree map into leaf nodes based on D
		TreeMap<Integer, List<DataEntry>> tempMap = new TreeMap<Integer, List<DataEntry>>();
		for (Entry<Integer, List<DataEntry>> entry : wholeMap.entrySet()) {
			if (tempMap.isEmpty() || tempMap.size() < D * 2)
				tempMap.put(entry.getKey(), entry.getValue());
			else {
				leafNodes.add(new leafNode(tempMap));
				tempMap = new TreeMap<Integer, List<DataEntry>>();
				tempMap.put(entry.getKey(), entry.getValue());
			}
		}

		// deal with the last one or two node(s).
		int remaining;
		if (wholeMap.isEmpty()) {
			return null;
		} else if (leafNodes.isEmpty()) {
			leafNodes.add(new leafNode(tempMap));
		} else if ((remaining = tempMap.size()) < D) {
			remaining += 2 * D;
			int k = remaining / 2;

			TreeMap<Integer, List<DataEntry>> lastMap = leafNodes.get(leafNodes.size() - 1).getMap();
			leafNodes.remove(leafNodes.size() - 1);
			for (int i = 0; i < 2 * D - k; i++) {
				int lastKey = lastMap.lastKey();
				List<DataEntry> lastEntryList = lastMap.get(lastKey);
				tempMap.put(lastKey, lastEntryList);
				lastMap.remove(lastKey);
			}

			leafNodes.add(new leafNode(lastMap));
			leafNodes.add(new leafNode(tempMap));
		} else {// 2D >= nodeMap.size() > D
			leafNodes.add(new leafNode(tempMap));
		}

		return leafNodes;
	}

	/**
	 * Build the first layer of index nodes at the beginning and then build the
	 * upper levels recursively
	 */
	private void buildIndexLayers() {
		// build the first index layer
		ArrayList<indexNode> firstIndexLayer = new ArrayList<indexNode>();

		ArrayList<leafNode> leafChildren = new ArrayList<leafNode>();
		ArrayList<Integer> keyList = new ArrayList<Integer>();
		if (leafNodes.isEmpty()) {
			root = null;
			return;
		}
		for (leafNode ln : leafNodes) {
			keyList.add(ln.getMap().firstKey());
			leafChildren.add(ln);
			if (keyList.size() == 2 * D + 1) {
				keyList.remove(0);
				firstIndexLayer.add(new indexNode(keyList, leafChildren));
				leafChildren = new ArrayList<leafNode>();
				keyList = new ArrayList<Integer>();
			}
		}

		// handle last one or two node
		if (firstIndexLayer.isEmpty()) {
			if (keyList.size() > 1)
				keyList.remove(0);
			else
				leafChildren.add(0, null);
			firstIndexLayer.add(new indexNode(keyList, leafChildren));
		} else if (keyList.size() > 0 && keyList.size() < D + 1) {
			int k = keyList.size() - 1 + 2 * D;
			// pop the last node from the layer
			indexNode tempNode = firstIndexLayer.get(firstIndexLayer.size() - 1);
			firstIndexLayer.remove(firstIndexLayer.size() - 1);
			ArrayList<leafNode> firstChildren = tempNode.getChildren();
			ArrayList<leafNode> secondChildren = new ArrayList<leafNode>();
			ArrayList<Integer> keyList1 = new ArrayList<Integer>();
			ArrayList<Integer> keyList2 = new ArrayList<Integer>();
			firstChildren.addAll(leafChildren);

			while (firstChildren.size() > k / 2) {
				secondChildren.add(firstChildren.get(k / 2));
				keyList2.add(firstChildren.get(k / 2).getMap().firstKey());
				firstChildren.remove(k / 2);
			}
			for (leafNode ln : firstChildren) {
				keyList1.add(ln.getMap().firstKey());
			}
			keyList1.remove(0);
			keyList2.remove(0);

			firstIndexLayer.add(new indexNode(keyList1, firstChildren));
			firstIndexLayer.add(new indexNode(keyList2, secondChildren));

		} else if (keyList.size() != 0) {// D <= size <= 2D
			keyList.remove(0);
			firstIndexLayer.add(new indexNode(keyList, leafChildren));
		}

		// after building the first layer, build the rest of the index
		// layers recursively until reach to root;
		buildUpperLayers(firstIndexLayer);
	}

	/**
	 * @param IndexLayer:
	 *            arrayList of index nodes build the upper level from the input
	 *            arrayList call itself recursively until reaches the root
	 */

	private void buildUpperLayers(ArrayList<indexNode> IndexLayer) {
		ArrayList<indexNode> output = new ArrayList<indexNode>();

		ArrayList<indexNode> indexChildren = new ArrayList<indexNode>();
		ArrayList<Integer> keyList = new ArrayList<Integer>();
		for (indexNode index : IndexLayer) {
			indexChildren.add(index);
			keyList.add(index.getKeys().get(0));
			if (keyList.size() == 2 * D + 1) {
				keyList.remove(0);
				output.add(new indexNode(keyList, indexChildren, true));
				keyList = new ArrayList<Integer>();
				indexChildren = new ArrayList<indexNode>();
			}
		}

		// if the last node underflow, pop the last node from output
		// merge the children between the two nodes, split them.
		// generate new keys for the new sets of children
		// add both new nodes to output
		if (output.isEmpty() && !keyList.isEmpty()) {
			if (keyList.size() > 1)
				keyList.remove(0);
			else
				indexChildren.add(0, null);
			root = new indexNode(keyList, indexChildren, true);
			return;
		} else if (!keyList.isEmpty() && keyList.size() - 1 < D) {
			indexNode tempNode = output.get(output.size() - 1);
			ArrayList<indexNode> firstChildren = tempNode.getIndexChildren();
			firstChildren.addAll(indexChildren);
			output.remove(output.size() - 1);
			// split firstChildren to make two new nodes
			int k = firstChildren.size();
			ArrayList<indexNode> secondChildren = new ArrayList<indexNode>();
			ArrayList<Integer> keyList1 = new ArrayList<Integer>();
			ArrayList<Integer> keyList2 = new ArrayList<Integer>();
			while (firstChildren.size() > k / 2) {
				secondChildren.add(firstChildren.get(k / 2));
				keyList2.add(firstChildren.get(k / 2).getKeys().get(0));
				firstChildren.remove(k / 2);
			}
			for (indexNode in : firstChildren) {
				keyList1.add(in.getKeys().get(0));
			}
			keyList1.remove(0);
			keyList2.remove(0);

			output.add(new indexNode(keyList1, firstChildren, true));
			output.add(new indexNode(keyList2, secondChildren, true));

		} else if (!keyList.isEmpty()) {
			keyList.remove(0);
			output.add(new indexNode(keyList, indexChildren, true));
		}
		// recursion
		buildUpperLayers(output);
	}

	/**
	 * get the root of this tree
	 * @return index node: root
	 */
	public indexNode getRoot(){
		return root;
	}
	
	/**
	 * get all leaf nodes
	 * 
	 * @return an arrayList of leaf node
	 */
	public ArrayList<leafNode> getAllChildren() {
		return leafNodes;
	}
}
