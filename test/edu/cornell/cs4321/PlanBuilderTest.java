package edu.cornell.cs4321;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.junit.Test;

import edu.cornell.cs4321.Database.DatabaseCatalog;
import edu.cornell.cs4321.IO.LogicalPlanWriter;
import edu.cornell.cs4321.IO.PhysicalPlanWriter;
import edu.cornell.cs4321.LogicalOperators.LogicalOperator;
import edu.cornell.cs4321.PhysicalOperators.Operator;
import edu.cornell.cs4321.UnionFind.Element;
import edu.cornell.cs4321.Visitors.PhysicalPlanBuilderVisitor;
import edu.cornell.cs4321.Visitors.UnionFindVisitor;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.parser.CCJSqlParser;
import net.sf.jsqlparser.parser.ParseException;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;

public class PlanBuilderTest {

	@Test
	public void test() throws ParseException, FileNotFoundException {
		String query = "SELECT * FROM R, S, T, U "+
					   "WHERE R.A != U.B AND R.A = S.B AND S.C = T.D AND R.A = 2 AND T.D = T.X AND U.Y != 42";
		InputStream stream = new ByteArrayInputStream(query.getBytes(StandardCharsets.UTF_8));
		CCJSqlParser parser = new CCJSqlParser(stream);
		Statement statement = parser.Statement();
		LogicalPlanBuilder pb = new LogicalPlanBuilder(statement);
		LogicalOperator op = pb.getRootLogicalOperator();
		System.out.println("hello");
		LogicalPlanWriter lpWriter = new LogicalPlanWriter("samples/query1_logicalPlan.txt");
		lpWriter.write(op);
	}
	
	@Test
	public void testAlias() throws ParseException, FileNotFoundException {
		String query = "SELECT DISTINCT S.A, R.G "+
					   "FROM Sailors S, Reserves R, Boats B "+
					   "WHERE S.B = R.G AND S.A = B.D AND R.H <> B.D AND R.H < 100 "+
					   "ORDER BY S.A";
		InputStream stream = new ByteArrayInputStream(query.getBytes(StandardCharsets.UTF_8));
		CCJSqlParser parser = new CCJSqlParser(stream);
		Statement statement = parser.Statement();
		LogicalPlanBuilder pb = new LogicalPlanBuilder(statement);
		LogicalOperator op = pb.getRootLogicalOperator();
		System.out.println("hello");
		LogicalPlanWriter lpWriter = new LogicalPlanWriter("samples/query2_logicalPlan.txt");
		lpWriter.write(op);
	}
	
	@Test
	public void simpleQuery() throws ParseException, FileNotFoundException {

		String query = "SELECT DISTINCT * FROM R";
		InputStream stream = new ByteArrayInputStream(query.getBytes(StandardCharsets.UTF_8));
		CCJSqlParser parser = new CCJSqlParser(stream);
		Statement statement = parser.Statement();
		LogicalPlanBuilder pb = new LogicalPlanBuilder(statement);
		LogicalOperator op = pb.getRootLogicalOperator();
		System.out.println("hello");
		LogicalPlanWriter lpWriter = new LogicalPlanWriter("samples/query3_logicalPlan.txt");
		lpWriter.write(op);
	}
	
	@Test
	public void selectionPhysicalPlanTest() throws ParseException, FileNotFoundException {
		String query = "SELECT Sailors.A FROM Sailors WHERE Sailors.A >= 100 and Sailors.A < 150 AND Sailors.B > 1000 ORDER BY Sailors.B";
		DatabaseCatalog.getInstance("samples/2/input");
		InputStream stream = new ByteArrayInputStream(query.getBytes(StandardCharsets.UTF_8));
		CCJSqlParser parser = new CCJSqlParser(stream);
		Statement statement = parser.Statement();
		LogicalPlanBuilder pb = new LogicalPlanBuilder(statement);
		LogicalOperator op = pb.getRootLogicalOperator();
		System.out.println("hello");
		LogicalPlanWriter lpWriter = new LogicalPlanWriter("samples/query_logicalPlan.txt");
		lpWriter.write(op);
		
		PhysicalPlanBuilderVisitor visitor = new PhysicalPlanBuilderVisitor(statement, "samples/2/tempSort");
		op.accept(visitor);
		Operator queryOperator = visitor.getOperator();
		PhysicalPlanWriter ppWriter = new PhysicalPlanWriter("samples/query_physicalPlan.txt");
		ppWriter.write(queryOperator);
		System.out.println("done.");
	}

}
