package edu.cornell.cs4321.Database;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

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
	private static HashMap<String, IndexInfo> indexMap = new HashMap<>();
	private static DatabaseCatalog instance = null;

	/**
	 * Constructor: Construct an DatabaseCatalog instance for the use of getInstance
	 * 
	 * @param inputDir
	 * schema txt file directory location  
	 */
	private DatabaseCatalog(String inputDir){
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
		if(fr != null){
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
					for(int i = 1; i<n; i++){
						Column c = new Column();
						c.setColumnName(tokens[i]);
						c.setTable(t);
						curSchemaList.add(c);
					}
					schemaMap.put(tokens[0], curSchemaList);
					String curTablePath = inputDir + "/db/data/" + tokens[0];
					tablePathMap.put(tokens[0], curTablePath);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		this.setIndexMap(inputDir);
	}
	
	
	/**
	 * Set the field indexMap, <tablename, column_with_index>
	 * @param inputDir
	 * index_info txt file directory location  
	 */
	private void setIndexMap(String inputDir){
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
					
					boolean isClustered = Boolean.parseBoolean(tokens[2]);
					int order = Integer.parseInt(tokens[3]);
					
					indexMap.put(tokens[0], new IndexInfo(c, isClustered, order));
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
	public static DatabaseCatalog getInstance(String inputDir){
		if(instance == null){
			instance = new DatabaseCatalog(inputDir);
		}
		return instance;
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
	public static IndexInfo getIndexInfoByTable(String tableName){
		return indexMap.get(tableName);
	}

	/**
	 * Set schemaMap by specific table and columns
	 * @param tableName table name
	 * @param schemaList A list of Columns of the table
     */
	public static void setSchemaByTable(String tableName, List<Column> schemaList) {
		schemaMap.put(tableName, schemaList);
	}
}
