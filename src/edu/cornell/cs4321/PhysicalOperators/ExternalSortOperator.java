package edu.cornell.cs4321.PhysicalOperators;

import java.io.File;
import java.util.ArrayList;
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
	private int fileCount;
	
	/**
	 * 
	 * @param childOperator
	 * @param orderByList
	 * @param nPages
	 */
	public ExternalSortOperator(Operator childOperator, List<OrderByElement> orderByList, int nPages, String tempDir) {
		this.childOperator = childOperator;
		sortedTupleList = new LinkedList<>();
		this.tempDir = tempDir;
		this.nPages = nPages;
		fileCount = 0;
		//tempSubDir is the directory for this ES operator
		UUID uuid = UUID.randomUUID();
		tempSubDirName = uuid.toString();
		new File(tempSubDirName).mkdir();
		
		if (orderByList != null) {
			for (OrderByElement e : orderByList) {
				Column c = (Column) e.getExpression();
				sortByColumns.add(c);
			}
		}
		
		pass0();
		mergePass();
	}

	
	private void pass0(){
		Tuple t = childOperator.getNextTuple();
		if (t != null) {
			int pageMaxSize = 1024 / t.getValues().size();
			while (t != null) {
				sortedTupleList.add(t);
				t = childOperator.getNextTuple();

				// if buffer is full, sort and write to file
				if (sortedTupleList.size() == pageMaxSize) {
					fileCount++;
					Collections.sort(sortedTupleList, new TupleComparator(sortByColumns));
					
					String outputDir = tempDir+"/"+tempSubDirName+"/interOutput"+fileCount;
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
				String outputDir = tempDir+"/"+tempSubDirName+"/interOutput"+fileCount;
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
	
	private void mergePass(){
		File folder = new File(tempDir+"/"+tempSubDirName);
		File[] fileArray = folder.listFiles();
		ArrayList<ArrayList<File>> allFiles = new ArrayList<ArrayList<File>>();
		int currBuffSize = 0;
		ArrayList<File> fileList;
		for (int i = 0; i < fileArray.length; i++){
			if(currBuffSize == 0){
				fileList = new ArrayList<File>();
			}
			fileList.add(fileArray)
		}
		
		
		TupleComparator tcmp = new TupleComparator(sortByColumns);
		if(isBinaryTuple){
			PriorityQueue<BinaryTupleReader> pq = new PriorityQueue<BinaryTupleReader>(nPages-1, 
					new Comparator<BinaryTupleReader>() {
		              	public int compare(BinaryTupleReader i, BinaryTupleReader j) {
		              		int res = tcmp.compare(i.readNextTuple(), j.readNextTuple());
		              		i.reset();
		              		j.reset();
		              		return res;
		              	}
	            	});
			BinaryTupleReader btr = new BinaryTupleReader();
		}else{
			
			
			
			
		}
	}
	
	/**
	 * 
	 */
	@Override
	public Tuple getNextTuple() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * 
	 */
	@Override
	public void reset() {
		// TODO Auto-generated method stub

	}

}
