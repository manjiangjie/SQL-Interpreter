package edu.cornell.cs4321;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.junit.Test;

import edu.cornell.cs4321.LogicalOperators.LogicalOperator;
import edu.cornell.cs4321.UnionFind.Element;
import edu.cornell.cs4321.Visitors.UnionFindVisitor;
import net.sf.jsqlparser.parser.CCJSqlParser;
import net.sf.jsqlparser.parser.ParseException;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;

public class LogicalPlanBuilderTest {

	@Test
	public void test() throws ParseException {
		String query = "SELECT * FROM R, S, T, U "+
					   "WHERE R.A != U.B AND R.A = S.B AND S.C = T.D AND R.A = 2 AND T.D = T.X AND U.Y != 42";
		InputStream stream = new ByteArrayInputStream(query.getBytes(StandardCharsets.UTF_8));
		CCJSqlParser parser = new CCJSqlParser(stream);
		Statement statement = parser.Statement();
		//LogicalPlanBuilder pb = new LogicalPlanBuilder(statement);
		//LogicalOperator op = pb.getRootLogicalOperator();
		UnionFindVisitor ufv = new UnionFindVisitor();
		Select select = (Select) statement;
        PlainSelect pSelect = (PlainSelect) select.getSelectBody();

        pSelect.getWhere().accept(ufv);
		
		for(Element e : ufv.getUnionFind().getUnionFind()){
			for(Column s : e.getAttribute()){
				System.out.print(s+", ");
			}
			System.out.println();
			System.out.println("equality: "+e.getEquality());
			System.out.println("lower: "+e.getLowerBound());
			System.out.println("upper: "+e.getUpperBound());
			System.out.println("================");
		}
		System.out.println("hello");
	}

}
