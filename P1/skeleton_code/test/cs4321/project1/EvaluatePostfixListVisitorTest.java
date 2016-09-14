package cs4321.project1;

import static org.junit.Assert.*;
import cs4321.project1.list.*;
import org.junit.Test;
public class EvaluatePostfixListVisitorTest {

	private static final double DELTA = 1e-15;

	@Test
	public void testSingleNumberNode() {
		ListNode n1 = new NumberListNode(1.0);
		EvaluatePostfixListVisitor v1 = new EvaluatePostfixListVisitor();
		n1.accept(v1);
		assertEquals(1.0, v1.getResult(), DELTA);
	}

	@Test
	public void testAdditionSimple() {
		ListNode n1 = new NumberListNode(1.0);
		ListNode n2 = new NumberListNode(2.0);
		ListNode n3 = new AdditionListNode();
		n1.setNext(n2);
		n2.setNext(n3);
		EvaluatePostfixListVisitor v1 = new EvaluatePostfixListVisitor();
		n1.accept(v1);
		assertEquals(3.0, v1.getResult(), DELTA);

		ListNode n4 = new NumberListNode(1.0);
		ListNode n5 = new NumberListNode(2.0);
		ListNode n6 = new AdditionListNode();
		n5.setNext(n4);
		n4.setNext(n6);
		EvaluatePostfixListVisitor v2 = new EvaluatePostfixListVisitor();
		n5.accept(v2);
		assertEquals(3.0, v2.getResult(), DELTA);
	}

	@Test
	public void testAdditionMultipleInstances() {
		ListNode n1 = new NumberListNode(1.0);
		ListNode n2 = new NumberListNode(2.0);
		ListNode n3 = new AdditionListNode();
		ListNode n4 = new NumberListNode(3.0);
		ListNode n5 = new AdditionListNode();
		n1.setNext(n2);
		n2.setNext(n3);
		n3.setNext(n4);
		n4.setNext(n5); //expression is 1 2 + 3 +
		EvaluatePostfixListVisitor v1 = new EvaluatePostfixListVisitor();
		n1.accept(v1);
		assertEquals(6.0, v1.getResult(), DELTA);
	}

	@Test
	public void testAdditionDivision(){
		ListNode n1 = new NumberListNode(5.0);
		ListNode n2 = new NumberListNode(5.0);
		ListNode n3 = new DivisionListNode();
		ListNode n4 = new NumberListNode(2.0);
		ListNode n5 = new AdditionListNode();

		n1.setNext(n2);
		n2.setNext(n5);

		EvaluatePostfixListVisitor v2 = new EvaluatePostfixListVisitor();
		n1.accept(v2);
		assertEquals(10.0, v2.getResult(), DELTA);

		n5.setNext(n4);
		n4.setNext(n3);

		EvaluatePostfixListVisitor v1 = new EvaluatePostfixListVisitor();
		n1.accept(v1);
		assertEquals(5.0, v1.getResult(), DELTA);
	}

	@Test
	public void testEverything(){
		ListNode n1 = new NumberListNode(4.0);
		ListNode n2 = new NumberListNode(1.0);
		ListNode n3 = new NumberListNode(2.0);
		ListNode n4 = new NumberListNode(3.0);
		ListNode n5 = new NumberListNode(6.0);
		ListNode n6 = new DivisionListNode();
		ListNode n7 = new AdditionListNode();
		ListNode n8 = new MultiplicationListNode();
		ListNode n9 = new UnaryMinusListNode();
		ListNode n10 = new AdditionListNode();

		n2.setNext(n3);
		n3.setNext(n7);
		n7.setNext(n1);
		n1.setNext(n8);

		EvaluatePostfixListVisitor v1 = new EvaluatePostfixListVisitor();
		n2.accept(v1);

		assertEquals(12.0, v1.getResult(), DELTA);

		n8.setNext(n5);
		n5.setNext(n10);

		n2.accept(v1);
		assertEquals(18.0, v1.getResult(), DELTA);

		n10.setNext(n4);
		n4.setNext(n6);

		n2.accept(v1);
		assertEquals(6.0, v1.getResult(), DELTA);
		n6.setNext(n9);
		n2.accept(v1);
		assertEquals(-6.0, v1.getResult(), DELTA);
	}
}