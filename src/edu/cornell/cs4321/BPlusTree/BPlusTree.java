package edu.cornell.cs4321.BPlusTree;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import edu.cornell.cs4321.Database.Tuple;
import edu.cornell.cs4321.IO.BinaryTupleReader;
import net.sf.jsqlparser.schema.Column;

public class BPlusTree {
	private indexNode root;
	private ArrayList<leafNode> leafNodes;
	private int D = 2;
	private boolean cluster;

	public BPlusTree(boolean cluster, String tableName, Column column) {
		this.cluster = cluster;
		if (cluster)
			sortCluster();
		leafNodes = buildLeafLayer(tableName, column);
		buildIndexLayers();
	}

	private void sortCluster() {

	}

	/**
	 * 
	 * @param tableName
	 * @param column
	 * @return
	 */
	private ArrayList<leafNode> buildLeafLayer(String tableName, Column column) {
		BinaryTupleReader btr = new BinaryTupleReader(tableName);
		Tuple firstTuple = btr.peek();
		int tupleSize = firstTuple.getSchema().size();
		int maxNumTuple = (4096 - 8) / (tupleSize * 4);
		int fileId = 0;
		int tupleId = 0;
		ArrayList<leafNode> leafNodes = new ArrayList<leafNode>();
		TreeMap<Integer, List<dataEntry>> nodeMap = new TreeMap<>();

		// scan all tuples and build the arrayList of nodes
		while (btr.peek() != null) {
			Tuple t = btr.readNextTuple();
			int key = t.getValueByCol(column);
			if (nodeMap.containsKey(key))
				nodeMap.get(key).add(new dataEntry(fileId, tupleId));
			else if (nodeMap.size() == 2 * D) {
				leafNodes.add(new leafNode(nodeMap));
				nodeMap = new TreeMap<Integer, List<dataEntry>>();
				ArrayList<dataEntry> tempList = new ArrayList<dataEntry>();
				tempList.add(new dataEntry(fileId, tupleId));
				nodeMap.put(key, tempList);
			} else {
				ArrayList<dataEntry> tempList = new ArrayList<dataEntry>();
				tempList.add(new dataEntry(fileId, tupleId));
				nodeMap.put(key, tempList);
			}
			tupleId++;
			if (tupleId == maxNumTuple) {
				fileId++;
				tupleId = 0;
			}
		}

		// deal with the last one or two node(s).
		int remaining;
		if (nodeMap.size() != 2 * D && (remaining = nodeMap.size()) < D) {
			remaining += 2 * D;
			int k = remaining / 2;

			TreeMap<Integer, List<dataEntry>> tempMap = leafNodes.get(leafNodes.size() - 1).getMap();
			leafNodes.remove(leafNodes.size() - 1);
			for (int i = 0; i < 2 * D - k; i++) {
				int lastKey = tempMap.lastKey();
				List<dataEntry> lastEntryList = tempMap.get(lastKey);
				nodeMap.put(lastKey, lastEntryList);
				tempMap.remove(lastKey);
			}

			leafNodes.add(new leafNode(tempMap));
			leafNodes.add(new leafNode(nodeMap));
		} else if (nodeMap.size() != D) {
			leafNodes.add(new leafNode(nodeMap));
		}

		return leafNodes;
	}

	private void buildIndexLayers() {
		// build the first index layer
		if (cluster) {
			ArrayList<indexNode> firstLayerIndex = new ArrayList<indexNode>();
			ArrayList<leafNode> indexChildren = new ArrayList<leafNode>();
			ArrayList<Integer> key = new ArrayList<Integer>();
			for (leafNode ln : leafNodes) {
				key.add(ln.getMap().firstKey());
				indexChildren.add(ln);
				if (key.size() == 2 * D + 1) {
					key.remove(0);
					firstLayerIndex.add(new indexNode(key, indexChildren));
					indexChildren = new ArrayList<leafNode>();
					key = new ArrayList<Integer>();
				}
			}
			
			
			// handle last one or two node
			if (key.size() < D && key.size() > 0) {
				int k = key.size() + 2 * D;
				indexNode tempNode = firstLayerIndex.get(firstLayerIndex.size() - 1);
				firstLayerIndex.remove(firstLayerIndex.size() - 1);
				for (int i = 0; i < 2 * D - k; i++) {
					ArrayList<leafNode> al = tempNode.getChildren();
					leafNode tempLeafNode = al.get(al.size() - 1);
					indexChildren.add(0, tempLeafNode);
					tempNode.getChildren().remove(al.size() - 1);
				}
				firstLayerIndex.add(tempNode);
				ArrayList<Integer> keylist = new ArrayList<Integer>();
				for (leafNode n : indexChildren) {
					keylist.add(n.getMap().firstKey());
				}
				keylist.remove(0);
				firstLayerIndex.add(new indexNode(keylist, indexChildren));
			} else if (key.size() != 0) {
				firstLayerIndex.add(new indexNode(key, indexChildren));
			}

			// after building the first layer, build the rest of the index
			// layers
			if(firstLayerIndex.size()==1){
				root = firstLayerIndex.get(0);
				return;
			}
			
			// build recursively until reach to root;
			buildUpperLayers(firstLayerIndex);
			
		} else {// uncluster
			System.out.println("need to implement uncluster");
		}
	}

	private void buildUpperLayers(ArrayList<indexNode> IndexLayer){
		ArrayList<indexNode> output = new ArrayList<indexNode>();
		
		ArrayList<indexNode> indexChildren = new ArrayList<indexNode>();
		ArrayList<Integer> keyList = new ArrayList<Integer>();
		for(indexNode index : IndexLayer){
			indexChildren.add(index);
			keyList.add(index.getKeys().get(0));
			if(keyList.size()==2*D+1){
				keyList.remove(0);
				output.add(new indexNode(keyList, indexChildren, true));
				keyList = new ArrayList<Integer>();
				indexChildren = new ArrayList<indexNode>();
			}
		}
		
		//if the last node underflow, pop the last node from output
		//merge the children between the two nodes, split them.
		//generate new keys for the new sets of children
		//add both new nodes to output
		if(!keyList.isEmpty()&&keyList.size()-1<D){
			indexNode tempNode = output.get(output.size()-1);
			ArrayList<indexNode> firstChildren = tempNode.getIndexChildren();
			firstChildren.addAll(indexChildren);
			output.remove(output.size()-1);
			//split firstChildren to make two new nodes
			int k = firstChildren.size();
			ArrayList<indexNode> secondChildren = new ArrayList<indexNode>();
			ArrayList<Integer> keyList1 = new ArrayList<Integer>();
			ArrayList<Integer> keyList2 = new ArrayList<Integer>();
			while(firstChildren.size()>k/2){
				secondChildren.add(firstChildren.get(k/2));
				keyList2.add(firstChildren.get(k/2).getKeys().get(0));
				firstChildren.remove(k/2);
			}
			for(indexNode in : firstChildren){
				keyList1.add(in.getKeys().get(0));
			}
			keyList1.remove(0);
			keyList2.remove(0);
			
			output.add(new indexNode(keyList1, firstChildren, true));
			output.add(new indexNode(keyList2, secondChildren, true));
			
		}else if(!keyList.isEmpty()){
			keyList.remove(0);
			output.add(new indexNode(keyList, indexChildren, true));
		}
		
		if(output.size()==1){
			root = output.get(0);
			return;
		}else{
			buildUpperLayers(output);
		}
	}
	
	public ArrayList<leafNode> getAllChildren() {
		return leafNodes;
	}
}
