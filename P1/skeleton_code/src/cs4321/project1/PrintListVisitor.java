package cs4321.project1;

import cs4321.project1.list.*;

/**
 * traverses a list representing an expression in either prefix or postfix form,
 * and creates a String that represents the expression.
 * 
 * @author Jiangjie Man; jm2559
 */

public class PrintListVisitor implements ListVisitor {

	private String result;

	public PrintListVisitor() {
		result = "";
	}

	/**
	 * Method to get the finished string representation when visitor is done
	 *
	 * @return string representation of the visited list
	 */
	public String getResult() {
		return result.substring(1);
	}

	/**
	 * Visit method for number list node; just concatenates the numeric value to the
	 * running string
	 *
	 * @param node
	 *            the node to be visited
	 */
	@Override
	public void visit(NumberListNode node) {
		result += " " + node.getData();
		ListNode p = node.getNext();
		if (p != null) {
			p.accept(this);
		}
	}

	/**
	 * Visit method for addition node based on list traversal
	 *
	 * @param node
	 *            the node to be visited
	 */
	@Override
	public void visit(AdditionListNode node) {
		result += " +";
		ListNode p = node.getNext();
		if (p != null) {
			p.accept(this);
		}
	}

	/**
	 * Visit method for subtraction node based on list traversal
	 *
	 * @param node
	 *            the node to be visited
	 */
	@Override
	public void visit(SubtractionListNode node) {
		result += " -";
		ListNode p = node.getNext();
		if (p != null) {
			p.accept(this);
		}
	}

	/**
	 * Visit method for multiplication node based on list traversal
	 *
	 * @param node
	 *            the node to be visited
	 */
	@Override
	public void visit(MultiplicationListNode node) {
		result += " *";
		ListNode p = node.getNext();
		if (p != null) {
			p.accept(this);
		}
	}

	/**
	 * Visit method for division node based on list traversal
	 *
	 * @param node
	 *            the node to be visited
	 */
	@Override
	public void visit(DivisionListNode node) {
		result += " /";
		ListNode p = node.getNext();
		if (p != null) {
			p.accept(this);
		}
	}

	/**
	 * Visit method for unary minus node; recursively visit sublist and wraps
	 * result in parens with unary ~
	 *
	 * @param node
	 *            the node to be visited
	 */
	@Override
	public void visit(UnaryMinusListNode node) {
		result += " ~";
		ListNode p = node.getNext();
		if (p != null) {
			p.accept(this);
		}
	}
}
