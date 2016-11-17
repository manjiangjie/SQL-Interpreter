package edu.cornell.cs4321;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import edu.cornell.cs4321.BPlusTree.BPlusTree;
import edu.cornell.cs4321.Database.*;
import edu.cornell.cs4321.IO.BinaryTupleWriter;
import edu.cornell.cs4321.IO.Converter;
import edu.cornell.cs4321.LogicalOperators.LogicalOperator;
import edu.cornell.cs4321.PhysicalOperators.*;
import edu.cornell.cs4321.Visitors.PhysicalPlanBuilderVisitor;
import net.sf.jsqlparser.parser.CCJSqlParser;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;

/**
 * This is the top-level class which contains main function. It reads queries
 * from input folder and write query results to output folder. During the
 * process, it calls JsqlParser to parse SQL query and QueryPlanBuilder to build
 * a query plan tree. Then it invokes getNextTuple() on the root operator of
 * query plan repeatedly until it retrieves all tuples.
 * 
 * @author Heng Kuang hk856, Hao Qian hq43, Jiangjie Man jm2559
 *
 */
public class Parser {

	public static void main(String[] args) {

		try {
			String configFile = args[0];
			BufferedReader configReader = new BufferedReader(new FileReader(configFile));
			String inputDir = configReader.readLine();
			String outputDir = configReader.readLine();
			String tempDir = configReader.readLine();
			boolean buildIndexes = (Integer.parseInt(configReader.readLine()) == 1);
			boolean evalQueries = (Integer.parseInt(configReader.readLine()) == 1);
			configReader.close();
			
			DatabaseCatalog.getInstance(inputDir);
			
			if(buildIndexes){
				buildIndexes(inputDir);
				System.out.println("finshed building index!");
			}
			
			if(evalQueries){
				evalQueries(inputDir, outputDir, tempDir);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void buildIndexes(String inputDir) throws IOException {
		String indexFilePath = inputDir + "/db/index_info.txt";
		BufferedReader indexInfoReader = new BufferedReader(new FileReader(indexFilePath));
		String configStr;
		while((configStr = indexInfoReader.readLine()) != null){
			String[] tokens = configStr.split("\\s+");
			String tableName = tokens[0];
			String columnName = tokens[1];
			boolean clustered = tokens[2].equals("1");
			int order = Integer.parseInt(tokens[3]);
			Column c = new Column();
			c.setColumnName(columnName);
			Table t = new Table();
			t.setName(tableName);
			c.setTable(t);
			BPlusTree indexTree = new BPlusTree(clustered, tableName, c, order, inputDir + "/db/");
		}
	}

	public static void evalQueries(String inputDir, String outputDir, String tempDir)
			throws IOException {
		// Initialize database catalog
		DatabaseCatalog.getInstance(inputDir);

		String queryFilePath = inputDir + "/query.sql";
		String configFilePath = inputDir + "/plan_builder_config.txt";

		BufferedReader br = new BufferedReader(new FileReader(queryFilePath));
		String queryStr = br.readLine().trim();
		int queryNumber = 1;

		BufferedReader br2 = new BufferedReader(new FileReader(configFilePath));
		String[] joinMethod = br2.readLine().split("\\s+");
		String[] sortMethod = br2.readLine().split("\\s+");
		boolean useIndex = (Integer.parseInt(br2.readLine())==1);
		br2.close();
		while (queryStr != null && queryStr.length() > 0) {
			try {
				String queryPath = outputDir + "/query" + queryNumber;
				BinaryTupleWriter btw = new BinaryTupleWriter(queryPath);
				long currentTime = System.currentTimeMillis();
				try {
					InputStream stream = new ByteArrayInputStream(queryStr.getBytes(StandardCharsets.UTF_8));
					CCJSqlParser parser = new CCJSqlParser(stream);
					Statement statement;
					if ((statement = parser.Statement()) != null) {
						// Construct query plan tree
						PhysicalPlanBuilderVisitor visitor = new PhysicalPlanBuilderVisitor(statement, joinMethod,
								sortMethod, tempDir, useIndex);
						LogicalOperator logicalOperator = visitor.getLogicalOperator();
						logicalOperator.accept(visitor);
						Operator queryOperator = visitor.getOperator();
						// Get tuples repeatedly
						Tuple t;
						while ((t = queryOperator.getNextTuple()) != null) {
							btw.writeNextTuple(t);
						}
					}

				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					btw.close();
					Converter converter = new Converter(queryPath);
					converter.writeToFile(queryPath + "_humanreadable");
					queryStr = br.readLine();
					double timing = (System.currentTimeMillis() - currentTime) / 1000.0;
					System.out.println("Finish processing query #" + queryNumber + ", " + timing + " seconds");
					queryNumber += 1;
					
					//TODO: add this at the end of every query scan
					File temp = new File(tempDir);
					deleteFolder(temp);
					
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		br.close();
	}
	
	//TODO: add this method
	private static void deleteFolder(File folder){
		File[] files = folder.listFiles();
		for(File f: files){
			if(f.isDirectory()) {
                deleteFolder(f);
                f.delete();
            } else {
                f.delete();
            }
		}
	}
	
}
