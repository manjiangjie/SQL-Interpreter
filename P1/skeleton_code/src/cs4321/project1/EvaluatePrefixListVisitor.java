package cs4321.project1;

import cs4321.project1.list.*;

import java.util.Stack;

/**
 * traverses a list representing an expression in prefix form,
 * and evaluates the expression to a single number.
 * 
 * @author Jiangjie Man; jm2559
 */

public class EvaluatePrefixListVisitor implements ListVisitor {

	private double result;
	private Stack<Double> operands;
	private Stack<char[]> operators;

	public EvaluatePrefixListVisitor() {
		operands = new Stack<>();
		operators = new Stack<>();
	}

	/**
	 * Method to get the evaluation number
	 *
	 * @return the evaluation number of string representation of the visited list
	 */
	public double getResult() {
		return result; // so that skeleton code compiles
	}

	/**
	 * Visit method for number list node; push the node to stack and evaluate
	 *
	 * @param node
	 *            the node to be visited
	 */
	@Override
	public void visit(NumberListNode node) {
		operands.push(node.getData());
		while (!operators.empty()) {
			char[] op = operators.pop();
			if (op[1] == '2') {
				op[1] = '1';
				operators.push(op);
				break;
			} else if (op[1] == '1') {
				double a = operands.pop();
				double b = operands.pop();
				if (op[0] == '+') {
					operands.push(a + b);
				} else if (op[0] == '-') {
					operands.push(b - a);
				} else if (op[0] == '*') {
					operands.push(a * b);
				} else if (op[0] == '/') {
					operands.push(b / a);
				} else if (op[0] == '~') {
					operands.push(b);
					operands.push(-a);
				}
			}
		}
		ListNode p = node.getNext();
		if (p == null) {
			result = operands.pop();
		} else {
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
		operators.push(new char[]{'+', '2'});
		node.getNext().accept(this);
	}

	/**
	 * Visit method for subtraction node based on list traversal
	 *
	 * @param node
	 *            the node to be visited
	 */
	@Override
	public void visit(SubtractionListNode node) {
		operators.push(new char[]{'-', '2'});
		node.getNext().accept(this);
	}

	/**
	 * Visit method for multiplication node based on list traversal
	 *
	 * @param node
	 *            the node to be visited
	 */
	@Override
	public void visit(MultiplicationListNode node) {
		operators.push(new char[]{'*', '2'});
		node.getNext().accept(this);
	}

	/**
	 * Visit method for division node based on list traversal
	 *
	 * @param node
	 *            the node to be visited
	 */
	@Override
	public void visit(DivisionListNode node) {
		operators.push(new char[]{'/', '2'});
		node.getNext().accept(this);
	}

	/**
	 * Visit method for unary minus node; recursively visit next node and pop the negative value off the stack
	 *
	 * @param node
	 *            the node to be visited
	 */
	@Override
	public void visit(UnaryMinusListNode node) {
		operators.push(new char[]{'~', '1'});
		node.getNext().accept(this);
	}
}
