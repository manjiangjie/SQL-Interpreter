package edu.cornell.cs4321;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.junit.Test;

import edu.cornell.cs4321.LogicalOperators.LogicalOperator;
import net.sf.jsqlparser.parser.CCJSqlParser;
import net.sf.jsqlparser.parser.ParseException;
import net.sf.jsqlparser.statement.Statement;

public class LogicalPlanBuilderTest {

	@Test
	public void test() throws ParseException {
		String query = "SELECT * FROM R, S, T, U "+
					   "WHERE R.A != U.B AND R.A = S.B AND S.C = T.D AND R.A = 2 AND T.D = T.X AND U.Y != 42";
		InputStream stream = new ByteArrayInputStream(query.getBytes(StandardCharsets.UTF_8));
		CCJSqlParser parser = new CCJSqlParser(stream);
		Statement statement = parser.Statement();
		LogicalPlanBuilder pb = new LogicalPlanBuilder(statement);
		LogicalOperator op = pb.getRootLogicalOperator();
		System.out.println("hello");
	}

}
