package edu.cornell.cs4321.BPlusTree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;

import edu.cornell.cs4321.Database.Tuple;
import edu.cornell.cs4321.Database.TupleComparator;
import edu.cornell.cs4321.IO.BPlusTreeSerializer;
import edu.cornell.cs4321.IO.BinaryTupleReader;
import edu.cornell.cs4321.IO.BinaryTupleWriter;
import net.sf.jsqlparser.schema.Column;

/**
 * This class constructs a B+ tree via bulk loading. Begin by scanning the relation file and
 * generating all the data entries in the format described above, in sorted order by key.
 *
 * @author Heng Kuang: hk856; Jiangjie Man: jm2559
 */
public class BPlusTree {
	private IndexNode root;
	private ArrayList<LeafNode> leafNodes;
	private int D;
	private int SIZE = 4096;
	private int size = 0;
	private BPlusTreeSerializer serializer;
	private String filePath;

	/**
	 * constructor of a B+ Tree. Will build a tree once get called.
	 *
	 * @param clustered clustered or unclustered
	 * @param tableName Relation name
	 * @param column Column for this relation
	 * @param order B+ tree order
	 * @param filePath the index file path to be written
	 */
	public BPlusTree(boolean clustered, String tableName, Column column, int order, String filePath) {
		this.D = order;
		this.filePath = filePath;
		serializer = new BPlusTreeSerializer(filePath + "indexes/" + tableName + "." + column.getColumnName());
		if (clustered) {
			sortCluster(tableName, column);
		}
		leafNodes = buildLeafLayer(tableName, column);
		//System.out.println(column.getColumnName());
		//System.out.println(leafNodes.size());
		root = buildIndexLayers();
		//System.out.println(size);
		serializer.writeNextNode(root);
		serializer.writeHeadPage(size, leafNodes.size(), D);
		serializer.close();
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
		List<Tuple> tl = new LinkedList<>();
		while (t != null) {
			tl.add(t);
			t = btr.readNextTuple();
		}
		List<Column> cl = new LinkedList<>();
		cl.add(column);
		Collections.sort(tl, new TupleComparator(cl));
		btr.deleteFile();
		BinaryTupleWriter btw = new BinaryTupleWriter(filePath + "data/" + tableName);
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
	private ArrayList<LeafNode> buildLeafLayer(String tableName, Column column) {
		BinaryTupleReader btr = new BinaryTupleReader(tableName);
		leafNodes = new ArrayList<LeafNode>();
		TreeMap<Integer, List<DataEntry>> wholeMap = new TreeMap<>();
		// scan all tuples and build a tree map to store all key/dataEntry pairs
		Tuple t = btr.readNextTuple();
		int key;
		while (t != null) {
			key = t.getValueByCol(column);
			int pageId = btr.getPageId();
			int tupleId = btr.getTupleId();
			if (wholeMap.containsKey(key))
				// if the node have key
				wholeMap.get(key).add(new DataEntry(pageId, tupleId));
			else {
				// map does not have the key but is not full
				ArrayList<DataEntry> tempList = new ArrayList<DataEntry>();
				tempList.add(new DataEntry(pageId, tupleId));
				wholeMap.put(key, tempList);
			}
			t = btr.readNextTuple();
		}

		// split the whole tree map into leaf nodes based on D
		TreeMap<Integer, List<DataEntry>> tempMap = new TreeMap<Integer, List<DataEntry>>();
		for (Entry<Integer, List<DataEntry>> entry : wholeMap.entrySet()) {
			if (tempMap.size() < D * 2) {
				tempMap.put(entry.getKey(), entry.getValue());
			} else {
				size += 1;
				LeafNode n = new LeafNode(tempMap, size);
				leafNodes.add(n);
				serializer.writeNextNode(n);
				tempMap = new TreeMap<>();
				tempMap.put(entry.getKey(), entry.getValue());
			}
		}

		// deal with the last one or two node(s).
		int remaining;
		LeafNode n = null;
		if (wholeMap.isEmpty()) {
			return null;
		} else if (leafNodes.isEmpty()) {
			size += 1;
			n = new LeafNode(tempMap, size);
		} else if ((remaining = tempMap.size()) < D) {
			remaining += 2 * D;
			int k = remaining / 2;

			TreeMap<Integer, List<DataEntry>> lastMap = leafNodes.get(leafNodes.size() - 1).getMap();
			leafNodes.remove(leafNodes.size() - 1);
			serializer.setPage(leafNodes.size() - 1);
			for (int i = 0; i < 2 * D - k; i++) {
				int lastKey = lastMap.lastKey();
				List<DataEntry> lastEntryList = lastMap.get(lastKey);
				tempMap.put(lastKey, lastEntryList);
				lastMap.remove(lastKey);
			}
			n = new LeafNode(lastMap, size);
			leafNodes.add(n);
			serializer.writeNextNode(n);
			size += 1;
			n = new LeafNode(tempMap, size);
		} else {// 2D >= nodeMap.size() > D
			size += 1;
			n = new LeafNode(tempMap, size);
		}
		leafNodes.add(n);
		serializer.writeNextNode(n);

		return leafNodes;
	}

	/**
	 * Build the first layer of index nodes at the beginning and then build the
	 * upper levels recursively
	 */
	private IndexNode buildIndexLayers() {
		// build the first index layer
		ArrayList<IndexNode> firstIndexLayer = new ArrayList<IndexNode>();

		ArrayList<LeafNode> leafChildren = new ArrayList<LeafNode>();
		ArrayList<Integer> keyList = new ArrayList<Integer>();
		if (leafNodes.isEmpty()) {
			return null;
		}
		for (LeafNode ln : leafNodes) {
			keyList.add(ln.getMap().firstKey());
			leafChildren.add(ln);
			if (keyList.size() == 2 * D + 1) {
				//TODO: fix
				int leafKey = keyList.get(0);
				keyList.remove(0);
				size += 1;
				IndexNode n = new IndexNode(keyList, leafChildren, size, leafKey);
				firstIndexLayer.add(n);
				serializer.writeNextNode(n);
				leafChildren = new ArrayList<LeafNode>();
				keyList = new ArrayList<Integer>();
			}
		}

		// handle last one or two node
		if (firstIndexLayer.isEmpty()) {
			//TODO: fix
			int leafKey = keyList.get(0);
			if (keyList.size() > 1) {
				keyList.remove(0);
			}
			
			size += 1;
			IndexNode n = new IndexNode(keyList, leafChildren, size, leafKey);
			firstIndexLayer.add(n);
			serializer.writeNextNode(n);
		} else if (keyList.size() > 0 && keyList.size() < D + 1) {

			// pop the last node from the layer
			IndexNode tempNode = firstIndexLayer.get(firstIndexLayer.size() - 1);
			firstIndexLayer.remove(firstIndexLayer.size() - 1);
			serializer.setPage(tempNode.getAddress());
			ArrayList<LeafNode> firstChildren = tempNode.getChildren();
			ArrayList<LeafNode> secondChildren = new ArrayList<LeafNode>();
			ArrayList<Integer> keyList1 = new ArrayList<Integer>();
			ArrayList<Integer> keyList2 = new ArrayList<Integer>();
			firstChildren.addAll(leafChildren);
			int k = firstChildren.size();
			while (firstChildren.size() > k / 2 ) {
				secondChildren.add(firstChildren.get(k / 2 ));
				keyList2.add(firstChildren.get(k / 2 ).getMap().firstKey());
				firstChildren.remove(k / 2 );
			}
			for (LeafNode ln : firstChildren) {
				keyList1.add(ln.getMap().firstKey());
			}
			
			//TODO: fix
			int leafKey1 = keyList1.get(0);
			int leafKey2 = keyList2.get(0);
			keyList1.remove(0);
			keyList2.remove(0);
			
			IndexNode n = new IndexNode(keyList1, firstChildren, size, leafKey1);
			firstIndexLayer.add(n);
			serializer.writeNextNode(n);
			size += 1;
			n = new IndexNode(keyList2, secondChildren, size, leafKey2);
			firstIndexLayer.add(n);
			serializer.writeNextNode(n);

		} else if (keyList.size() != 0) {// D <= size <= 2D
			int leafKey = keyList.get(0);
			keyList.remove(0);
			size += 1;
			firstIndexLayer.add(new IndexNode(keyList, leafChildren, size, leafKey));
		}
		// after building the first layer, build the rest of the index
		// layers recursively until reach to root;
		return buildUpperLayers(firstIndexLayer);
	}

	/**
	 * @param IndexLayer:
	 *            arrayList of index nodes build the upper level from the input
	 *            arrayList call itself recursively until reaches the root
	 */


	private IndexNode buildUpperLayers(ArrayList<IndexNode> IndexLayer) {
		ArrayList<IndexNode> output = new ArrayList<IndexNode>();

		ArrayList<IndexNode> indexChildren = new ArrayList<IndexNode>();

		ArrayList<Integer> keyList = new ArrayList<Integer>();
		for (IndexNode index : IndexLayer) {
			indexChildren.add(index);
			keyList.add(index.getLeafKey());
			if (keyList.size() == 2 * D + 1) {
				
				int leafKey = keyList.remove(0);
				size += 1;
				IndexNode n = new IndexNode(keyList, indexChildren, true, size, leafKey);
				output.add(n);
				serializer.writeNextNode(n);
				keyList = new ArrayList<Integer>();
				indexChildren = new ArrayList<IndexNode>();
			}
		}
		// if the last node underflow, pop the last node from output
		// merge the children between the two nodes, split them.
		// generate new keys for the new sets of children
		// add both new nodes to output
		if (output.isEmpty() && !keyList.isEmpty()) {
			int leafKey = keyList.get(0);
			if (keyList.size() > 1) {
				keyList.remove(0);
			}
			size += 1;
			return new IndexNode(keyList, indexChildren, true, size, leafKey);
		} else if (!keyList.isEmpty() && keyList.size() - 1 < D) {
			IndexNode tempNode = output.get(output.size() - 1);
			ArrayList<IndexNode> firstChildren = tempNode.getIndexChildren();

			firstChildren.addAll(indexChildren);
			output.remove(output.size() - 1);
			serializer.setPage(tempNode.getAddress());
			// split firstChildren to make two new nodes
			int k = firstChildren.size();
			ArrayList<IndexNode> secondChildren = new ArrayList<IndexNode>();
			ArrayList<Integer> keyList1 = new ArrayList<Integer>();
			ArrayList<Integer> keyList2 = new ArrayList<Integer>();
			while (firstChildren.size() > k / 2) {
				secondChildren.add(firstChildren.get(k / 2));
				keyList2.add(firstChildren.get(k / 2).getLeafKey());
				firstChildren.remove(k / 2);
			}
			for (IndexNode in : firstChildren) {
				keyList1.add(in.getLeafKey());
			}
			int leafKey1 = keyList1.remove(0);
			int leafKey2 = keyList2.remove(0);
			IndexNode n = new IndexNode(keyList1, indexChildren, true, size, leafKey1);
			output.add(n);
			serializer.writeNextNode(n);
			size += 1;
			n = new IndexNode(keyList2, indexChildren, true, size, leafKey2);
			output.add(n);
			serializer.writeNextNode(n);

		} else if (!keyList.isEmpty()) {
			int leafKey = keyList.remove(0);
			size += 1;
			IndexNode n = new IndexNode(keyList, indexChildren, true, size, leafKey);
			output.add(n);
			serializer.writeNextNode(n);
		}
		
		// recursion
		return buildUpperLayers(output);
	}

	/**
	 * get the root of this tree
	 * @return index node: root
	 */
	public IndexNode getRoot(){
		return root;
	}
	
	/**
	 * get all leaf nodes
	 * 
	 * @return an arrayList of leaf node
	 */
	public ArrayList<LeafNode> getAllChildren() {
		return leafNodes;
	}
}