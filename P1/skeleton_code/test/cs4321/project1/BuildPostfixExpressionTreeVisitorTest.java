package cs4321.project1;

import static org.junit.Assert.*;

import org.junit.Test;

import cs4321.project1.list.*;
import cs4321.project1.tree.*;

public class BuildPostfixExpressionTreeVisitorTest {

	private static final double DELTA = 1e-15;

	@Test
	public void testSingleLeafNode() {
		TreeNode n1 = new LeafTreeNode(1.0);
		BuildPostfixExpressionTreeVisitor v1 = new BuildPostfixExpressionTreeVisitor();
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

		BuildPostfixExpressionTreeVisitor v1 = new BuildPostfixExpressionTreeVisitor();
		n3.accept(v1);
		ListNode result = v1.getResult();
		assertTrue(result instanceof NumberListNode);
		assertEquals(((NumberListNode) result).getData(), 1.0, DELTA);
		result = result.getNext();
		assertTrue(result instanceof NumberListNode);
		assertEquals(((NumberListNode) result).getData(), 2.0, DELTA);
		result = result.getNext();
		assertTrue(result instanceof AdditionListNode);
		assertNull(result.getNext());

		BuildPostfixExpressionTreeVisitor v2 = new BuildPostfixExpressionTreeVisitor();
		n4.accept(v2);
		result = v2.getResult();
		assertTrue(result instanceof NumberListNode);
		assertEquals(((NumberListNode) result).getData(), 2.0, DELTA);
		result = result.getNext();
		assertTrue(result instanceof NumberListNode);
		assertEquals(((NumberListNode) result).getData(), 1.0, DELTA);
		result = result.getNext();
		assertTrue(result instanceof AdditionListNode);
		assertNull(result.getNext());
	}

    @Test
	public void testUnaryMinusNode() {
		TreeNode n1 = new LeafTreeNode(1.0);
		TreeNode n2 = new UnaryMinusTreeNode(n1);

		BuildPostfixExpressionTreeVisitor v1 = new BuildPostfixExpressionTreeVisitor();
		n2.accept(v1);
		ListNode result = v1.getResult();
		assertTrue(result instanceof NumberListNode);
		assertEquals(((NumberListNode) result).getData(), 1.0, DELTA);
		result = result.getNext();
		assertTrue(result instanceof UnaryMinusListNode);
		assertNull(result.getNext());

	}

	@Test
	public void testAdditionMultiplication(){
		TreeNode n1 = new LeafTreeNode(4.0);
		TreeNode n2 = new LeafTreeNode(1.0);
		TreeNode n3 = new LeafTreeNode(2.0);
		TreeNode n4 = new AdditionTreeNode(n2, n3);
		TreeNode n5 = new MultiplicationTreeNode(n4,n1);

		BuildPostfixExpressionTreeVisitor v1 = new BuildPostfixExpressionTreeVisitor();
		n5.accept(v1);

		ListNode result = v1.getResult();

		assertTrue(result instanceof NumberListNode);
		assertEquals(((NumberListNode) result).getData(), 1.0, DELTA);
		result = result.getNext();
		assertTrue(result instanceof NumberListNode);
		assertEquals(((NumberListNode) result).getData(), 2.0, DELTA);

		result = result.getNext();
		assertTrue(result instanceof AdditionListNode);

		result = result.getNext();
		assertTrue(result instanceof NumberListNode);
		assertEquals(((NumberListNode) result).getData(), 4.0, DELTA);

		result = result.getNext();
		assertTrue(result instanceof MultiplicationListNode);
		assertNull(result.getNext());

	}

	@Test
	public void testEverything(){
		TreeNode n1 = new LeafTreeNode(4.0);
		TreeNode n2 = new LeafTreeNode(1.0);
		TreeNode n3 = new LeafTreeNode(2.0);
		TreeNode n4 = new LeafTreeNode(3.0);
		TreeNode n5 = new LeafTreeNode(6.0);
		TreeNode n6 = new AdditionTreeNode(n2, n3);
		TreeNode n7 = new MultiplicationTreeNode(n6, n1);
		TreeNode n8 = new AdditionTreeNode(n7, n5);
		TreeNode n9 = new DivisionTreeNode(n8, n4);
		TreeNode n10 = new UnaryMinusTreeNode(n9);

		BuildPostfixExpressionTreeVisitor v1 = new BuildPostfixExpressionTreeVisitor();
		n10.accept(v1);

		ListNode result = v1.getResult();

		assertTrue(result instanceof NumberListNode);
		assertEquals(((NumberListNode) result).getData(), 1.0, DELTA);
		result = result.getNext();
		assertTrue(result instanceof NumberListNode);
		assertEquals(((NumberListNode) result).getData(), 2.0, DELTA);

		result = result.getNext();
		assertTrue(result instanceof AdditionListNode);

		result = result.getNext();
		assertTrue(result instanceof NumberListNode);
		assertEquals(((NumberListNode) result).getData(), 4.0, DELTA);

		result = result.getNext();
		assertTrue(result instanceof MultiplicationListNode);

		result = result.getNext();
		assertTrue(result instanceof NumberListNode);
		assertEquals(((NumberListNode) result).getData(), 6.0, DELTA);

		result = result.getNext();
		assertTrue(result instanceof AdditionListNode);

		result = result.getNext();
		assertTrue(result instanceof NumberListNode);
		assertEquals(((NumberListNode) result).getData(), 3.0, DELTA);

		result = result.getNext();
		assertTrue(result instanceof DivisionListNode);

		result = result.getNext();
		assertTrue(result instanceof UnaryMinusListNode);
		assertNull(result.getNext());
	}

}
