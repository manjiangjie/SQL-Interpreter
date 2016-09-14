package cs4321.project1;

import static org.junit.Assert.*;

import org.junit.Test;

import cs4321.project1.tree.*;
import cs4321.project1.list.*;

public class BuildPrefixExpressionTreeVisitorTest {

	private static final double DELTA = 1e-15;

	@Test
	public void testSingleLeafNode() {
		TreeNode n1 = new LeafTreeNode(1.0);
        BuildPrefixExpressionTreeVisitor v1 = new BuildPrefixExpressionTreeVisitor();
		n1.accept(v1);
		ListNode result = v1.getResult();
		assertNull(result.getNext());
		assertTrue(result instanceof NumberListNode);
	}
	
	@Test
	public void testAdditionNode() {
		TreeNode n1 = new LeafTreeNode(1.0);
		TreeNode n2 = new LeafTreeNode(2.0);
		TreeNode n3 = new AdditionTreeNode(n1, n2);
		TreeNode n4 = new AdditionTreeNode(n2, n1);
		
        BuildPrefixExpressionTreeVisitor v1 = new BuildPrefixExpressionTreeVisitor();
		n3.accept(v1);
		ListNode result = v1.getResult();
		assertTrue(result instanceof AdditionListNode);
		result = result.getNext();
		assertTrue(result instanceof NumberListNode);
		assertEquals(((NumberListNode) result).getData(), 1.0, DELTA);
		result = result.getNext();
		assertTrue(result instanceof NumberListNode);
		assertEquals(((NumberListNode) result).getData(), 2.0, DELTA);
		
		
        BuildPrefixExpressionTreeVisitor v2 = new BuildPrefixExpressionTreeVisitor();
		n4.accept(v2);
		result = v2.getResult();
		assertTrue(result instanceof AdditionListNode);
		result = result.getNext();
		assertTrue(result instanceof NumberListNode);
		assertEquals(((NumberListNode) result).getData(), 2.0, DELTA);
		result = result.getNext();
		assertTrue(result instanceof NumberListNode);
		assertEquals(((NumberListNode) result).getData(), 1.0, DELTA);
	}
	
	@Test
	public void testUnaryMinusNode() {
		TreeNode n1 = new LeafTreeNode(1.0);
		TreeNode n2 = new UnaryMinusTreeNode(n1);
		
        BuildPrefixExpressionTreeVisitor v1 = new BuildPrefixExpressionTreeVisitor();
		n2.accept(v1);
		ListNode result = v1.getResult();
		assertTrue(result instanceof UnaryMinusListNode);
		ListNode first = result.getNext();
		assertTrue(first instanceof NumberListNode);
		assertEquals(((NumberListNode) first).getData(), 1.0, DELTA);
	}

	@Test
	public void testSubtractionMultiplication() {
		TreeNode n1 = new LeafTreeNode(4.0);
		TreeNode n2 = new LeafTreeNode(3.0);
		TreeNode n3 = new LeafTreeNode(2.0);
		TreeNode n4 = new SubtractionTreeNode(n1, n3);
		TreeNode n5 = new MultiplicationTreeNode(n4, n2);

		BuildPrefixExpressionTreeVisitor v1 = new BuildPrefixExpressionTreeVisitor();
		n5.accept(v1);

		ListNode result = v1.getResult();

		assertTrue(result instanceof MultiplicationListNode);

		result = result.getNext();
		assertTrue(result instanceof SubtractionListNode);

		result = result.getNext();
		assertTrue(result instanceof NumberListNode);
		assertEquals(((NumberListNode) result).getData(), 4.0, DELTA);

		result = result.getNext();
		assertTrue(result instanceof NumberListNode);
		assertEquals(((NumberListNode) result).getData(), 2.0, DELTA);

		result = result.getNext();
		assertTrue(result instanceof NumberListNode);
		assertEquals(((NumberListNode) result).getData(), 3.0, DELTA);
		assertNull(result.getNext());

	}

	@Test
	public void testAdditionDivision() {
		TreeNode n1 = new LeafTreeNode(8.0);
		TreeNode n2 = new LeafTreeNode(9.0);
		TreeNode n3 = new LeafTreeNode(10.0);
		TreeNode n4 = new AdditionTreeNode(n1, n3);
		TreeNode n5 = new DivisionTreeNode(n4, n2);

		BuildPrefixExpressionTreeVisitor v1 = new BuildPrefixExpressionTreeVisitor();
		n5.accept(v1);

		ListNode result = v1.getResult();

		assertTrue(result instanceof DivisionListNode);

		result = result.getNext();
		assertTrue(result instanceof AdditionListNode);

		result = result.getNext();
		assertTrue(result instanceof NumberListNode);
		assertEquals(((NumberListNode) result).getData(), 8.0, DELTA);

		result = result.getNext();
		assertTrue(result instanceof NumberListNode);
		assertEquals(((NumberListNode) result).getData(), 10.0, DELTA);

		result = result.getNext();
		assertTrue(result instanceof NumberListNode);
		assertEquals(((NumberListNode) result).getData(), 9.0, DELTA);
		assertNull(result.getNext());

	}

}