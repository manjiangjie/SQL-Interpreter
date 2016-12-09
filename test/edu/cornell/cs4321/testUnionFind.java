package edu.cornell.cs4321;

import java.util.Collections;

import org.junit.Test;

import edu.cornell.cs4321.UnionFind.Element;
import edu.cornell.cs4321.Visitors.UnionFindVisitor;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.NotEqualsTo;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;

public class testUnionFind {
	@Test
	public void test() {
		
		Table R = new Table();
		R.setName("R");
		Table S = new Table();
		S.setName("S");
		Table T = new Table();
		T.setName("T");
		Table U = new Table();
		U.setName("U");
		
		
		Column RA = new Column();
		RA.setColumnName("A");
		RA.setTable(R);
		
		Column UB = new Column();
		UB.setColumnName("B");
		UB.setTable(U);
		
		Column SB = new Column();
		SB.setColumnName("B");
		SB.setTable(S);
		
		Column SC = new Column();
		SC.setColumnName("C");
		SC.setTable(S);
		
		Column TD = new Column();
		TD.setColumnName("D");
		TD.setTable(T);
		
		Column TX = new Column();
		TX.setColumnName("X");
		TX.setTable(T);
		
		Column UY = new Column();
		UY.setColumnName("Y");
		UY.setTable(U);
		
		NotEqualsTo notEqualsTo = new NotEqualsTo();
		notEqualsTo.setLeftExpression(RA);
		notEqualsTo.setRightExpression(UB);
		
		EqualsTo equalTo = new EqualsTo();
		equalTo.setLeftExpression(RA);
		equalTo.setRightExpression(SB);
		
		AndExpression and1 = new AndExpression(notEqualsTo, equalTo);
		
		EqualsTo equalTo2 = new EqualsTo();
		equalTo2.setLeftExpression(SC);
		equalTo2.setRightExpression(TD);
		
		//left root
		AndExpression and2 = new AndExpression(and1, equalTo2);
		
		EqualsTo equalTor2 = new EqualsTo();
		equalTor2.setLeftExpression(RA);
		equalTor2.setRightExpression(new LongValue(2));
		AndExpression andr2 = new AndExpression(and2, equalTor2);
		
		EqualsTo equalTor = new EqualsTo();
		equalTor.setLeftExpression(TD);
		equalTor.setRightExpression(TX);
		
		AndExpression andr = new AndExpression(andr2, equalTor);
		
		
		NotEqualsTo notEqualsTo2 = new NotEqualsTo();
		notEqualsTo2.setLeftExpression(UY);
		notEqualsTo2.setRightExpression(new LongValue(42));
		
		AndExpression root = new AndExpression(andr, notEqualsTo2);
		
		UnionFindVisitor ufv = new UnionFindVisitor();
		root.accept(ufv);
		
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
		
		
		
	}
}
