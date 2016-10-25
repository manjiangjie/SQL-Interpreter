package edu.cornell.cs4321;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import edu.cornell.cs4321.Database.*;
import edu.cornell.cs4321.IO.BinaryTupleWriter;
import edu.cornell.cs4321.IO.Converter;
import edu.cornell.cs4321.LogicalOperators.LogicalOperator;
import edu.cornell.cs4321.PhysicalOperators.*;
import edu.cornell.cs4321.Visitors.PhysicalPlanBuilderVisitor;
import net.sf.jsqlparser.parser.CCJSqlParser;
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
		String inputDir = args[0];
		String outputDir = args[1];
		String tempDir = args[2];

		// Initialize database catalog
		DatabaseCatalog.getInstance(inputDir);
		
		String queryFilePath = inputDir + "/queries.sql";
		String configFilePath = inputDir + "/plan_builder_config.txt";
		try {
			BufferedReader br = new BufferedReader(new FileReader(queryFilePath));
			String queryStr = br.readLine();
			int queryNumber = 1;
			
			BufferedReader br2 = new BufferedReader(new FileReader(configFilePath));
			String[] joinMethod = br2.readLine().split("\\s+");
			String[] sortMethod = br2.readLine().split("\\s+");
			br2.close();
			while (queryStr != null) {
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
							PhysicalPlanBuilderVisitor visitor = 
									new PhysicalPlanBuilderVisitor(statement,joinMethod,sortMethod,tempDir);
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
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
