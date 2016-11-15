/**
 * 
 */
package edu.cornell.cs4321.IO;

import edu.cornell.cs4321.Database.DatabaseCatalog;
import edu.cornell.cs4321.Database.IndexInfo;
import edu.cornell.cs4321.Database.Tuple;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.LinkedList;
import java.util.List;
import edu.cornell.cs4321.BPlusTree.DataEntry;

/**
 * @author Hao Qian hq43
 * 
 * Deserialize BPlusTree index.
 */
public class BPlusTreeDeserializer {
	
	private FileInputStream fin;
	private FileChannel fc;
	private ByteBuffer bb;
	private static int SIZE = 4096;
	private int treeRootAddr;
	private int numLeaves;
	
	public BPlusTreeDeserializer(IndexInfo indexInfo) {
		String indexFilePath = indexInfo.getIndexPath();
		try {
            fin = new FileInputStream(indexFilePath);
            fc = fin.getChannel();
            bb = ByteBuffer.allocate(SIZE);
    		bb.clear();
    		fc.read(bb); // Read header page which stores metadata about the BPlusTree.
    		treeRootAddr = bb.getInt(0);
    		numLeaves = bb.getInt(4); 
        } catch (IOException e) {
            e.printStackTrace();
        }
	}
	
	
	/**
	 * Find all data entries which match the specified range.
	 * @return a list all matched data entries
	 * TODO instead of reading all entries into memory, try to retrieve an entry once using Queue.
	 */
	public List<DataEntry> getDataEntriesByIndex(Long lowkey, Long highkey, Boolean lowOpen, Boolean highOpen) {
		List<DataEntry> results = new LinkedList<>();
		if(lowkey!=null){
			int pageNum = locateMatchedNode(lowkey);
			boolean isLeaf = (bb.getInt()==0); // leaf/index flag
			
			while(isLeaf){
				int numDataEntries = bb.getInt();
				
				for (int i = 0; i < numDataEntries; i++) {
					int k = bb.getInt();
					if( lowOpen.booleanValue() && (k>lowkey.intValue()) ||
						!lowOpen.booleanValue() && (k>=lowkey.intValue()) ) {
						
						if( highkey!=null ){
							if( highOpen.booleanValue() && (k>=highkey.intValue()) ||
								!highOpen.booleanValue() && (k>lowkey.intValue()) ){
								return results;
							}
						}
						
						int numRids = bb.getInt();
						for (int j = 0; j < numRids; j++) {
							int pageId = bb.getInt();
							int tupleId = bb.getInt();
							DataEntry entry = new DataEntry(pageId, tupleId);
							results.add(entry);
						}
					}
				}
				
				pageNum++;
				if(pageNum > numLeaves) return results; // arrive at index nodes;
				try {
					fc.position((long)(pageNum * SIZE));
					bb = ByteBuffer.allocate(SIZE);
					bb.clear();				
					fc.read(bb);
				} catch (IOException e) {
					e.printStackTrace();
				}
				isLeaf = (bb.getInt()==0);
			}			
			
		} else {
			int pageNum = 1; // start from the first leaf node.
			while(pageNum <= numLeaves){
				try {
					fc.position((long)(pageNum * SIZE));
					bb = ByteBuffer.allocate(SIZE);
					bb.clear();				
					fc.read(bb);
				} catch (IOException e) {
					e.printStackTrace();
				}				
				boolean isLeaf = (bb.getInt()==0);
				int numDataEntries = bb.getInt();				
				for (int i = 0; i < numDataEntries; i++) {
					int k = bb.getInt();
					if( highOpen.booleanValue() && (k>=highkey.intValue()) ||
					    !highOpen.booleanValue() && (k>highkey.intValue()) ) {
						return results;
					}
					
					int numRids = bb.getInt();
					for (int j = 0; j < numRids; j++) {
						int pageId = bb.getInt();
						int tupleId = bb.getInt();
						DataEntry entry = new DataEntry(pageId, tupleId);
						results.add(entry);
					}
				}
			}
		}
		return results;
	}
	
	/**
	 * Find the leaf node which has the matched key, get prepared to retrieve data entries.
	 * @return page number of the leaf node.
	 */
	private int locateMatchedNode(Long key) {
		boolean isLeaf = false;
		int pageNum = treeRootAddr;
		try {
			fc.position((long)(pageNum * SIZE));
			bb = ByteBuffer.allocate(SIZE);
			bb.clear();			
			fc.read(bb);
		} catch (IOException e) {
			e.printStackTrace();
		}
		// Search from root to leaf
		while(!isLeaf) {
			int numKeys = bb.getInt(4);
			int childIdx;
			for(childIdx=0; childIdx<numKeys; childIdx++){
				int keyInNode = bb.getInt(8+4*childIdx);
				if(keyInNode >= key.intValue())
					break;
			}
			pageNum = bb.getInt(8+4*numKeys+4*childIdx);
			try {
				fc.position((long)(pageNum * SIZE));
				bb = ByteBuffer.allocate(SIZE);
				bb.clear();				
				fc.read(bb);
			} catch (IOException e) {
				e.printStackTrace();
			}
			isLeaf = (bb.getInt(0) == 0);
		}
		return pageNum;
	}
	
	public DataEntry getLeftMostEntry(Long lowkey, Boolean lowOpen, Long highkey, Boolean highOpen) {
		if(lowkey!=null){
			int pageNum = locateMatchedNode(lowkey);
			boolean isLeaf = (bb.getInt()==0); // leaf/index flag			
			while(isLeaf){
				int numDataEntries = bb.getInt();
				
				for (int i = 0; i < numDataEntries; i++) {
					int k = bb.getInt();
					if( lowOpen.booleanValue() && (k>lowkey.intValue()) ||
						!lowOpen.booleanValue() && (k>=lowkey.intValue()) ) {
						
						if( highkey!=null ){
							if( highOpen.booleanValue() && (k>=highkey.intValue()) ||
								!highOpen.booleanValue() && (k>lowkey.intValue()) ){
								return null;
							}
						}
						
						int numRids = bb.getInt();
						int pageId = bb.getInt();
						int tupleId = bb.getInt();
						return new DataEntry(pageId, tupleId);
					}
				}
				
				pageNum++;
				if(pageNum > numLeaves) return null; // arrive at index nodes;
				try {
					fc.position((long)(pageNum * SIZE));
					bb = ByteBuffer.allocate(SIZE);
					bb.clear();				
					fc.read(bb);
				} catch (IOException e) {
					e.printStackTrace();
				}
				isLeaf = (bb.getInt()==0);
			}
			return null;
			
		} else {
			int pageNum = 1; // start from the first leaf node.
			try {
				fc.position((long)(pageNum * SIZE));
				bb = ByteBuffer.allocate(SIZE);
				bb.clear();				
				fc.read(bb);
			} catch (IOException e) {
				e.printStackTrace();
			}
			int k = bb.getInt(2*4);
			if( highOpen.booleanValue() && (k>=highkey.intValue()) ||
				!highOpen.booleanValue() && (k>lowkey.intValue()) ){
					return null;
			}
			int pageId = bb.getInt(4*4);
			int tupleId = bb.getInt(4*5);
			return new DataEntry(pageId, tupleId);
		}
	}
}
