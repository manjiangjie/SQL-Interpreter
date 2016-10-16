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
import edu.cornell.cs4321.Operators.*;
import net.sf.jsqlparser.parser.CCJSqlParser;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.*;

/**
 * This is the top-level class which contains main function. It reads queries
 * from input folder and write query results to output folder. During the
 * process, it calls JsqlParser to parse SQL query and QueryPlanBuilder to build
 * a query plan tree. Then it invokes getNextTuple() on the root operator of
 * query plan repeatedly until it retrieves all tuples.
 * 
 * @author Chenxi Su cs2238, Hao Qian hq43, Jiangjie Man jm2559
 *
 */
public class Parser {

	public static void main(String[] args) {
		String inputDir = args[0];
		String outputDir = args[1];
		//String tempDir = args[2];

		// Initialize database catalog
		DatabaseCatalog.getInstance(inputDir);
		
		String queryFilePath = inputDir + "/queries.sql";
		BufferedReader br;
		String queryStr = null;
		try {
			br = new BufferedReader(new FileReader(queryFilePath));
			queryStr = br.readLine();
			int queryNumber = 1;
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
							Select select = (Select) statement;
							PlainSelect pSelect = (PlainSelect) select.getSelectBody();

							// Construct query plan tree
							QueryPlanBuilder qb = new QueryPlanBuilder(pSelect);
							Operator queryOperator = qb.getRootOperator();

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
						queryNumber = queryNumber + 1;
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
