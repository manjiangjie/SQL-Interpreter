package edu.cornell.cs4321.PhysicalOperators;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.UUID;

import edu.cornell.cs4321.Database.Tuple;
import edu.cornell.cs4321.Database.TupleComparator;
import edu.cornell.cs4321.IO.BinaryTupleReader;
import edu.cornell.cs4321.IO.BinaryTupleWriter;
import edu.cornell.cs4321.IO.StandardTupleReader;
import edu.cornell.cs4321.IO.StandardTupleWriter;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.OrderByElement;

public class ExternalSortOperator extends Operator {

	private List<Tuple> sortedTupleList;
	private Operator childOperator;
	private List<Column> sortByColumns;
	private boolean isBinaryTuple=true;
	private String tempDir;
	private String tempSubDirName;
	private int nPages;
	private List<Column> schemaList;
	private BinaryTupleReader binaryReader;
	private StandardTupleReader standardReader;
	
	/**
	 * 
	 * @param childOperator
	 * @param orderByList
	 * @param nPages
	 */
	//credit: http://www.ashishsharma.me/2011/08/external-merge-sort.html
	public ExternalSortOperator(Operator childOperator, List<OrderByElement> orderByList, int nPages, String tempDir) {
		this.childOperator = childOperator;
		sortedTupleList = new LinkedList<>();
		sortByColumns = new LinkedList<>();
		this.tempDir = tempDir;
		this.nPages = nPages;
		//tempSubDir is the directory for this ES operator
		UUID uuid = UUID.randomUUID();
		tempSubDirName = uuid.toString();
		new File(tempDir+"/"+tempSubDirName).mkdir();
		
		if (orderByList != null) {
			for (OrderByElement e : orderByList) {
				Column c = (Column) e.getExpression();
				sortByColumns.add(c);
			}
		}
		
		pass0();
		mergePass(1);
		
		File folder = new File(tempDir+"/"+tempSubDirName);
		if(folder.listFiles().length==0)return;
		File lastFile = folder.listFiles()[0];
		if (folder.listFiles().length>1){
			System.out.println("merge unfinished");
			return;
		}
		if(isBinaryTuple){
			binaryReader = new BinaryTupleReader(lastFile, schemaList);
		}else{
			standardReader = new StandardTupleReader(lastFile, schemaList);
		}
	}

	
	private void pass0(){
		Tuple t = childOperator.getNextTuple();
		if (t != null) {
			schemaList = t.getSchema();
			int pageMaxSize = 1024 / t.getValues().size();
			int fileCount = 0 ;
			while (t != null) {
				sortedTupleList.add(t);
				t = childOperator.getNextTuple();

				// if buffer is full, sort and write to file
				if (sortedTupleList.size() == pageMaxSize) {
					fileCount++;
					
					Collections.sort(sortedTupleList, new TupleComparator(sortByColumns));
					
					String outputDir = tempDir+"/"+tempSubDirName+"/phase0Output"+fileCount;
					if (isBinaryTuple) {
						BinaryTupleWriter bw = new BinaryTupleWriter(outputDir);
						for (Tuple tempTuple : sortedTupleList) {
							bw.writeNextTuple(tempTuple);
						}
						bw.close();
					}else{
						StandardTupleWriter sw = new StandardTupleWriter(outputDir);
						for (Tuple tempTuple : sortedTupleList) {
							sw.writeNextTuple(tempTuple);
						}
						sw.close();
					}
					
					sortedTupleList.clear();
				}
			}
			if(!sortedTupleList.isEmpty()){
				fileCount++;
				Collections.sort(sortedTupleList, new TupleComparator(sortByColumns));
				String outputDir = tempDir+"/"+tempSubDirName+"/phase0Output"+fileCount;
				if (isBinaryTuple) {
					BinaryTupleWriter bw = new BinaryTupleWriter(outputDir);
					for (Tuple tempTuple : sortedTupleList) {
						bw.writeNextTuple(tempTuple);
					}
					bw.close();
				}else{
					StandardTupleWriter sw = new StandardTupleWriter(outputDir);
					for (Tuple tempTuple : sortedTupleList) {
						sw.writeNextTuple(tempTuple);
					}
					sw.close();
				}
				
				sortedTupleList.clear();
			}
		}
	}
	
	/**
	 * Recursively read all files available in the the directory and do B-1 way merge
	 * until there is only one file left
	 */
	private void mergePass(int level) {
		File folder = new File(tempDir+"/"+tempSubDirName);
		//file Array hold all files
		File[] fileArray = folder.listFiles();
		if(fileArray.length==1)return;
		
		//store B-1 files in a list(buffer)
		//store all buffers into another list
		ArrayList<ArrayList<File>> allFiles = new ArrayList<ArrayList<File>>();
		
		ArrayList<File> fileList = new ArrayList<File>();
		for (int i = 0; i < fileArray.length; i++){
			fileList.add(fileArray[i]);
			if(fileList.size() == nPages-1 || i == fileArray.length-1){
				allFiles.add(fileList);
				fileList = new ArrayList<File>();
			}
		}
		
		//put a buffer into a priority queue
		//poll the queue until it is empty and move on to the next buffer
		TupleComparator tcmp = new TupleComparator(sortByColumns);
		
		if(isBinaryTuple){
			PriorityQueue<BinaryTupleReader> readerQueue = new PriorityQueue<BinaryTupleReader>(nPages-1, 
					new Comparator<BinaryTupleReader>() {
		              	public int compare(BinaryTupleReader i, BinaryTupleReader j) {
		              		int res = tcmp.compare(i.peek(), j.peek());
		              		return res;
		              	}
	            	});
			
			int fileCount = 0;
			for (ArrayList<File> fileBuffer : allFiles){
				//add all pages of a buffer into the queue
				for (File f : fileBuffer){
					readerQueue.add(new BinaryTupleReader(f, schemaList));
				}
				fileCount++;
				String outputDir = tempDir+"/"+tempSubDirName+"/interOutput"+level+fileCount;
				BinaryTupleWriter btw = new BinaryTupleWriter(outputDir);
				try{
					while(!readerQueue.isEmpty()){
						BinaryTupleReader smallestReader = readerQueue.poll();
						Tuple smallestTuple = smallestReader.readNextTuple();
						btw.writeNextTuple(smallestTuple);
						if(smallestReader.peek()==null){
							//reader queue is empty, delete the original file
							smallestReader.deleteFile();
						}else{
							readerQueue.add(smallestReader);
						}
					}
				}finally{
					btw.close();
					//TODO: Close the reader??
				}				
			}
			
			//call itself recursively
			mergePass(level+1);
			
		}else{
			PriorityQueue<StandardTupleReader> readerQueue = new PriorityQueue<StandardTupleReader>(nPages-1, 
					new Comparator<StandardTupleReader>() {
		              	public int compare(StandardTupleReader i, StandardTupleReader j) {
		              		int res = tcmp.compare(i.peek(), j.peek());
		              		return res;
		              	}
	            	});
			
			int fileCount = 0;
			for (ArrayList<File> fileBuffer : allFiles){
				//add all pages of a buffer into the queue
				for (File f : fileBuffer){
					readerQueue.add(new StandardTupleReader(f, schemaList));
				}
				fileCount++;
				String outputDir = tempDir+"/"+tempSubDirName+"/interOutput"+level+fileCount;
				StandardTupleWriter btw = new StandardTupleWriter(outputDir);
				try{
					while(!readerQueue.isEmpty()){
						StandardTupleReader smallestReader = readerQueue.poll();
						Tuple smallestTuple = smallestReader.readNextTuple();
						btw.writeNextTuple(smallestTuple);
						if(smallestReader.peek()==null){
							//reader queue is empty, delete the original file
							smallestReader.deleteFile();
						}else{
							readerQueue.add(smallestReader);
						}
					}
				}finally{
					btw.close();
				}				
			}
			
			//call itself recursively	
				mergePass(level+1);

			
		}
	}
	
	/**
	 * delete the sorted file in the file system
	 * 
	 * **/
	public void deleteFileFolder(){
		File folder = new File(tempDir+"/"+tempSubDirName);
		folder.delete();
	}
	
	/**
	 * @return the next tuple in the sorted file
	 */
	@Override
	public Tuple getNextTuple() {
		if(isBinaryTuple){
			
			return binaryReader==null ? null : binaryReader.readNextTuple();
		}else{
			return standardReader==null ? null : standardReader.readNextTuple();
		}
	}

	/**
	 * reset the tuple reader
	 */
	@Override
	public void reset() {
		if(isBinaryTuple){
			binaryReader.reset();
		}else{
			standardReader.reset();
		}
	}
}
