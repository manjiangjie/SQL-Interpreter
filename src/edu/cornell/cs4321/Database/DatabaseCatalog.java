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
	private static DatabaseCatalog instance = null;

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
				int numTuples = 0;
				Tuple t;
				Map<Column, int[]> stats = new HashMap<>();
				for (Column c : schemaMap.get(table)) {
					stats.put(c, new int[]{Integer.MAX_VALUE, Integer.MIN_VALUE});
				}
				while ((t = btr.readNextTuple()) != null) {
					numTuples += 1;
					for (Column c : stats.keySet()) {
						int value = t.getValueByCol(c);
						if (value < stats.get(c)[0]) {
							stats.put(c, new int[]{value, stats.get(c)[1]});
						}
						if (value > stats.get(c)[1]) {
							stats.put(c, new int[]{stats.get(c)[0], value});
						}
					}
				}
				bw.write(table + " " + Integer.toString(numTuples));
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
}
