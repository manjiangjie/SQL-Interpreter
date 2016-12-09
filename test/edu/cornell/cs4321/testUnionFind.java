package edu.cornell.cs4321;

import java.util.Collections;

import org.junit.Test;

import edu.cornell.cs4321.UnionFind.Element;
import edu.cornell.cs4321.Visitors.UnionFindVisitor;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;

public class testUnionFind {
	@Test
	public void test() {
		
		Column c1 = new Column();
		c1.setColumnName("A");
		Table table1 = new Table();
		table1.setName("S");
		c1.setTable(table1);
		
		Column c2 = new Column();
		c2.setColumnName("D");
		Table table2 = new Table();
		table2.setName("R");
		c2.setTable(table2);
		
		EqualsTo equalTo = new EqualsTo();
		equalTo.setLeftExpression(c1);
		LongValue l = new LongValue(100);
		equalTo.setRightExpression(l);
		
		EqualsTo equalTo2 = new EqualsTo();
		equalTo2.setLeftExpression(c1);
		equalTo2.setRightExpression(c2);
		
		
		AndExpression and = new AndExpression(equalTo, equalTo2);
		
		UnionFindVisitor ufv = new UnionFindVisitor();
		and.accept(ufv);
		
		for(Element e : ufv.getUnionFind().getUnionFind()){
			for( Column s : e.getAttribute()){
				System.out.print(s+" ");
			}
			System.out.println();
			System.out.println("equality: "+e.getEquality());
			System.out.println("lower: "+e.getLowerBound());
			System.out.println("upper: "+e.getUpperBound());
			System.out.println("================");
		}
		
	}
}
