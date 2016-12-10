package edu.cornell.cs4321;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import edu.cornell.cs4321.BPlusTree.BPlusTree;
import edu.cornell.cs4321.Database.*;
import edu.cornell.cs4321.IO.BinaryTupleWriter;
import edu.cornell.cs4321.IO.Converter;
import edu.cornell.cs4321.IO.DataGenerator;
import edu.cornell.cs4321.IO.LogicalPlanWriter;
import edu.cornell.cs4321.IO.PhysicalPlanWriter;
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
			configReader.close();
			
			DatabaseCatalog.getInstance(inputDir);

			//DataGenerator generator = new DataGenerator(inputDir);
			
			buildIndexes();
			System.out.println("finished building index!");
			
			evalQueries(inputDir, outputDir, tempDir);
			System.out.println("finished evaluating queries!");

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void buildIndexes() throws IOException {
		for (List<IndexInfo> indexInfoList : DatabaseCatalog.getIndexInfoList()) {
			for (IndexInfo indexInfo : indexInfoList) {
				BPlusTree indexTree = new BPlusTree(indexInfo);
				//Converter converter = new Converter(indexInfo.getIndexPath());
				//converter.indexConverter(indexTree.getRoot(), indexInfo);
			}
		}
	}

	public static void evalQueries(String inputDir, String outputDir, String tempDir)
			throws IOException {
		// Initialize database catalog
		DatabaseCatalog.getInstance(inputDir);

		String queryFilePath = inputDir + "/queries.sql";
		
		BufferedReader br = new BufferedReader(new FileReader(queryFilePath));
		String queryStr = br.readLine().trim();
		int queryNumber = 1;
		
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
						// Construct Logical Plan
						LogicalPlanBuilder logicalPlan = new LogicalPlanBuilder(statement);
						LogicalOperator logicalOperator = logicalPlan.getRootLogicalOperator();
						LogicalPlanWriter lpWriter = new LogicalPlanWriter(queryPath + "_logicalplan");
						lpWriter.write(logicalOperator);
						PhysicalPlanBuilderVisitor visitor = new PhysicalPlanBuilderVisitor(statement, tempDir);
						logicalOperator.accept(visitor);
						Operator queryOperator = visitor.getOperator();
						PhysicalPlanWriter ppWriter = new PhysicalPlanWriter(queryPath + "_physicalplan");
						ppWriter.write(queryOperator);
						// Get tuples repeatedly
						Tuple t;
						while ((t = queryOperator.getNextTuple()) != null) {
							t = new Tuple(t, statement);
							btw.writeNextTuple(t);
						}
					}

				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					btw.close();
					Converter converter = new Converter(queryPath);
					converter.tupleConverter(queryPath + "_humanreadable");
					queryStr = br.readLine();
					double timing = (System.currentTimeMillis() - currentTime) / 1000.0;
					System.out.println("Finish processing query #" + queryNumber + ", " + timing + " seconds");
					queryNumber += 1;

					File temp = new File(tempDir);
					deleteFolder(temp);
					
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		br.close();
	}

	private static void deleteFolder(File folder){
		File[] files = folder.listFiles();
		if (files != null) {
			for (File f : files) {
				if (f.isDirectory()) {
					deleteFolder(f);
					f.delete();
				} else {
					f.delete();
				}
			}
		}
	}
	
}
