package cs4321.project1;

import static org.junit.Assert.*;

import org.junit.Test;

import cs4321.project1.tree.TreeNode;

public class ParserTest {

	/*
	 * This class depends on the correct functioning of PrintTreeVisitor(), which is provided for you.
	 */
			
	@Test
	public void testSingleNumber() {
		Parser p1 = new Parser("1.0");
		TreeNode parseResult1 = p1.parse();
		PrintTreeVisitor v1 = new PrintTreeVisitor();
		parseResult1.accept(v1);
		assertEquals("1.0", v1.getResult());

	}
	
	@Test
	public void testUnaryMinusSimple() {
		Parser p1 = new Parser("- 1.0");
		TreeNode parseResult1 = p1.parse();
		PrintTreeVisitor v1 = new PrintTreeVisitor();
		parseResult1.accept(v1);
		assertEquals("(-1.0)", v1.getResult());

	}
	
	@Test
	public void testUnaryMinusComplex() {
		Parser p1 = new Parser("- - 1.0");
		TreeNode parseResult1 =  p1.parse();
		PrintTreeVisitor v1 = new PrintTreeVisitor();
		parseResult1.accept(v1);
		assertEquals("(-(-1.0))", v1.getResult());

	}

	@Test
	public void testAdditionAndSubtractionComplex() {
		Parser p1 = new Parser("1.0 + 2.0 + 3.0");
		TreeNode parseResult1 = p1.parse();
		PrintTreeVisitor v1 = new PrintTreeVisitor();
		parseResult1.accept(v1);
		assertEquals("((1.0+2.0)+3.0)", v1.getResult());

		Parser p2 = new Parser("( ( ( 1.0 + 2.0 ) ) )");
		TreeNode parseResult2 = p2.parse();
		PrintTreeVisitor v2 = new PrintTreeVisitor();
		parseResult2.accept(v2);
		assertEquals("(1.0+2.0)", v2.getResult());

		Parser p3 = new Parser("( ( ( 5.0 + 2.0 ) + 6.0 ) + 7.0 )");
		TreeNode parseResult3 = p3.parse();
		PrintTreeVisitor v3 = new PrintTreeVisitor();
		parseResult3.accept(v3);
		assertEquals("(((5.0+2.0)+6.0)+7.0)", v3.getResult());
	}

	@Test
	public void testMultiplicationAndDivisionComplex() {
		Parser p1 = new Parser("6.0 / ( 2.0 * 3.0 )");
		TreeNode parseResult1 = p1.parse();
		PrintTreeVisitor v1 = new PrintTreeVisitor();
		parseResult1.accept(v1);
		assertEquals("(6.0/(2.0*3.0))", v1.getResult());

		Parser p2 = new Parser("( ( 5.0 * ( 1.0 + 2.0 ) ) / 2.0 )");
		TreeNode parseResult2 = p2.parse();
		PrintTreeVisitor v2 = new PrintTreeVisitor();
		parseResult2.accept(v2);
		assertEquals("((5.0*(1.0+2.0))/2.0)", v2.getResult());

		Parser p3 = new Parser("1.0 + 2.0 * 3.0 + 4.0");
		TreeNode parseResult3 = p3.parse();
		PrintTreeVisitor v3 = new PrintTreeVisitor();
		parseResult3.accept(v3);
		assertEquals("((1.0+(2.0*3.0))+4.0)", v3.getResult());
	}

	@Test
	public void testUnaryMinusParenthesis() {
		Parser p1 = new Parser("- ( 5.0 + 6.0 )");
		TreeNode parseResult1 = p1.parse();
		PrintTreeVisitor v1 = new PrintTreeVisitor();
		parseResult1.accept(v1);
		assertEquals("(-(5.0+6.0))", v1.getResult());
	}

}
