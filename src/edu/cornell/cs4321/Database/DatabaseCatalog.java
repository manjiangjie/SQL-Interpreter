package edu.cornell.cs4321.Database;

import java.io.*;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import edu.cornell.cs4321.IO.BinaryTupleReader;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;

/**
 * Construct database with schema and data from local file
 * Use it by calling DatabaseCatalog.getInstance()
 *
 * @author Chenxi Su cs2238, Hao Qian hq43, Jiangjie Man jm2559
 */
public class DatabaseCatalog {
	
	private static HashMap<String, List<Column>> schemaMap = new HashMap<>();
	private static HashMap<String, String> tablePathMap = new HashMap<>();
	private static HashMap<String, List<IndexInfo>> indexMap = new HashMap<>();
	private static HashMap<String, List<Column>> fullSchemaMap = new HashMap<>();
	private static HashMap<String, Integer> numTuples = new HashMap<>();
	private static Map<Column, int[]> stats = new HashMap<>();
	private static DatabaseCatalog instance = null;
	private static final int pageSize = 4096;

	/**
	 * Constructor: Construct an DatabaseCatalog instance for the use of getInstance
	 * 
	 * @param inputDir
	 * schema txt file directory location  
	 */
	private DatabaseCatalog(String inputDir) {
		String schemaDir = inputDir + "/db/schema.txt";
		FileReader fr;
		BufferedReader br;
		//Initialize file reader
		try {
			 fr = new FileReader(schemaDir);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.out.println("Table not found! Please check your input");
			fr = null;
		}
		
		//Initialize SchemaMap
		if(fr != null) {
			br = new BufferedReader(fr);
			String curLine;
			try {
				while((curLine = br.readLine()) != null){
					//System.out.println(curLine);
					String[] tokens = curLine.split("\\s+");
					int n = tokens.length;
					//System.out.println(n);
					List<Column> curSchemaList = new LinkedList<Column>();
					Table t = new Table();
					t.setName(tokens[0]);
					for(int i = 1; i < n; i++) {
						Column c = new Column();
						c.setColumnName(tokens[i]);
						c.setTable(t);
						curSchemaList.add(c);
					}
					fullSchemaMap.put(tokens[0], curSchemaList);
					schemaMap.put(tokens[0], curSchemaList);
					String curTablePath = inputDir + "/db/data/" + tokens[0];
					tablePathMap.put(tokens[0], curTablePath);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		statistics(inputDir);
		setIndexMap(inputDir);
	}
	
	
	/**
	 * Set the field indexMap, <tablename, column_with_index>
	 * @param inputDir
	 * index_info txt file directory location  
	 */
	private void setIndexMap(String inputDir) {
		String indexDir = inputDir + "/db/index_info.txt";
		FileReader fr;
		BufferedReader br;
		//Initialize file reader
		try {
			 fr = new FileReader(indexDir);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.out.println("Index info not found! Please check your input");
			fr = null;
		}
		
		//Initialize SchemaMap
		if(fr != null){
			br = new BufferedReader(fr);
			String curLine;
			try {
				while((curLine = br.readLine()) != null){
					String[] tokens = curLine.split("\\s+");
					Table t = new Table();
					t.setName(tokens[0]);
					Column c = new Column();
					c.setColumnName(tokens[1]);
					c.setTable(t);
					
					boolean isClustered = (tokens[2].equals("1"));
					int order = Integer.parseInt(tokens[3]);
					String indexPath = inputDir + "/db/";
					if (indexMap.containsKey(tokens[0])) {
						indexMap.get(tokens[0]).add(new IndexInfo(c, isClustered, order, indexPath));
					} else {
						List<IndexInfo> indexList = new LinkedList<>();
						indexList.add(new IndexInfo(c, isClustered, order, indexPath));
						indexMap.put(tokens[0], indexList);
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Acquired an DatabaseCatalog instance
	 * 
	 * @param inputDir
	 * schema txt file directory location
	 * @return the singleton instance of DatabaseCatalog
	 */
	public static DatabaseCatalog getInstance(String inputDir) {
		if(instance == null){
			instance = new DatabaseCatalog(inputDir);
		}
		return instance;
	}

	/**
	 * Gather and write statistics about data in a stats.txt file
	 */
	public static void statistics(String inputDir) {
		try {
			FileWriter fw = new FileWriter(inputDir + "/db/stats.txt");
			BufferedWriter bw = new BufferedWriter(fw);
			for (String table : tablePathMap.keySet()) {
				BinaryTupleReader btr = new BinaryTupleReader(table);
				int count = 0;
				Tuple t;
				for (Column c : schemaMap.get(table)) {
					stats.put(c, new int[]{Integer.MAX_VALUE, Integer.MIN_VALUE});
				}
				while ((t = btr.readNextTuple()) != null) {
					count += 1;
					for (Column c : schemaMap.get(table)) {
						int value = t.getValueByCol(c);
						if (value < stats.get(c)[0]) {
							stats.put(c, new int[]{value, stats.get(c)[1]});
						}
						if (value > stats.get(c)[1]) {
							stats.put(c, new int[]{stats.get(c)[0], value});
						}
					}
				}
				numTuples.put(table, count);
				bw.write(table + " " + Integer.toString(count));
				for (Column c : schemaMap.get(table)) {
					bw.write(" " + c.getColumnName() + "," + Integer.toString(stats.get(c)[0]) +
							"," + Integer.toString(stats.get(c)[1]));
				}
				bw.write("\n");
			}
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Getter method for number of tuples in a relation
	 * @param t Table t
	 * @return number of tuples
     */
	public static int getNumTuples(String t) {
		return numTuples.get(t);
	}

	/**
	 * Getter method for statistics about relations
	 * @return relation statistics
     */
	public static Map<Column, int[]> getStats() {
		return stats;
	}
	
	/**
	 * Get the relative path of the table 
	 * @param tableName String of table name
	 * @return a string representing the relative path of the table
	 * */		
	public static String getPathByTableName(String tableName){
		return tablePathMap.get(tableName);
	}

	/**
	 * get the schema of the specified table in parameter
	 * @param tableName String of table name
	 * @return A list of Columns of the table, with each column referencing the Table object
	 * */	
	public static List<Column> getSchemaByTable(String tableName){
		return schemaMap.get(tableName);
	}
	
	/**
	 * get the index information of the specified table in parameter
	 * @param tableName String of table name
	 * @return an IndexInfo object of the specified table.
	 * */	
	public static List<IndexInfo> getIndexInfoByTable(String tableName){
		return indexMap.get(tableName);
	}

	public static List<List<IndexInfo>> getIndexInfoList() {
		return new LinkedList<>(indexMap.values());
	}

	/**
	 * Set schemaMap by specific table and columns
	 * @param tableName table name
	 * @param schemaList A list of Columns of the table
     */
	public static void setSchemaByTable(String tableName, List<Column> schemaList) {
		schemaMap.put(tableName, schemaList);
	}
	
	/**
	 * Reset schemaMap so that there is no alias
	 */
	public static void resetSchemaMap() {
		schemaMap = fullSchemaMap;
	}
	
	public static int getCostOfScan(String tableName) {
		return getNumPages(tableName);
	}
	
	public static int getNumPages(String tableName) {
		int nTuples = numTuples.get(tableName);
		int nCols = schemaMap.get(tableName).size();
		return (int) Math.ceil((nTuples * nCols * 4.0) / pageSize); 
	}
	
	public static int getNumLeaves(String tableName, String columnName) {
		for(IndexInfo idxInfo : indexMap.get(tableName)) {
			if(idxInfo.getColumn().getColumnName().equals(columnName)) {
				return idxInfo.getNumLeaves();
			}
		}
		return 0;
	}
	
	public static double getReductionFactorClosed(String tableName, String columnName, Long lowKey, Long highKey) {
		Boolean lowOpen = null, highOpen = null;
		if(lowKey != null) {
			lowOpen = (Boolean)false;
		}
		if(highKey != null) {
			highOpen = (Boolean)false;
		}
		return getReductionFactor(tableName, columnName, lowKey, highKey, lowOpen, highOpen);
	}
	
	public static double getReductionFactor(String tableName, String columnName, Long lowKey, Long highKey, Boolean lowOpen, Boolean highOpen) {
		int low = Integer.MIN_VALUE;
		int high = Integer.MAX_VALUE;
		for(Column c : stats.keySet()) {
			if(c.getColumnName().equals(columnName) && c.getTable().getName().equals(tableName)) {
				low = stats.get(c)[0];
				high = stats.get(c)[1];
				break;
			}
		}
		double range = (double)(high - low + 1);
		
		if(lowKey != null) {
			lowKey = lowOpen.booleanValue() ? (lowKey+1) : lowKey;
			lowKey = Math.max(lowKey, low);
		}
		if(highKey != null) {
			highKey = highOpen.booleanValue() ? (highKey-1) : highKey;
			highKey = Math.min(highKey, high);
		}
		
		if(lowKey == null && highKey == null) {
			return 1.0;
		}
		else if(lowKey == null && highKey != null) {
			return (highKey - low + 1) / range;
		}
		else if(lowKey != null && highKey == null) {
			return (high - lowKey + 1) / range;
		}
		else {
			return (highKey - lowKey + 1) / range;
		}
	}
}
