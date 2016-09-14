package cs4321.project1;

import cs4321.project1.list.*;
import java.util.Stack;

/**
 * Traverses an expression tree and builds up a running list in postfix order.
 * (We traverse the left subtree, then the right subtree before ending at the root.)
 *
 * @author Albert Leung; al2237
 */
public class EvaluatePostfixListVisitor implements ListVisitor {
	private Stack<Double> postfix_stack;

	public EvaluatePostfixListVisitor() {
		// TODO fill me in
		postfix_stack = new Stack<>();
	}

	public double getResult() {
		// TODO fill me in
		return postfix_stack.pop(); // so that skeleton code compiles
	}

	private void next_node(ListNode node){
		if(node.getNext() != null){
			node.getNext().accept(this);
		}
	}

	@Override
	public void visit(NumberListNode node) {
		// TODO fill me in
		postfix_stack.push(node.getData());
		next_node(node);
	}

	@Override
	public void visit(AdditionListNode node) {
		// TODO fill me in
		double operand_one = postfix_stack.pop();
		double operand_two = postfix_stack.pop();
		postfix_stack.push(operand_one + operand_two);
		next_node(node);

	}

	@Override
	public void visit(SubtractionListNode node) {
		// TODO fill me in
		double operand_one = postfix_stack.pop();
		double operand_two = postfix_stack.pop();
		postfix_stack.push(operand_two - operand_one);
		next_node(node);
	}

	@Override
	public void visit(MultiplicationListNode node) {
		// TODO fill me in
		double operand_one = postfix_stack.pop();
		double operand_two = postfix_stack.pop();
		postfix_stack.push(operand_one * operand_two);
		next_node(node);
	}

	@Override
	public void visit(DivisionListNode node) {
		// TODO fill me in
		double operand_one = postfix_stack.pop();
		double operand_two = postfix_stack.pop();
		postfix_stack.push(operand_two / operand_one);
		next_node(node);
	}

	@Override
	public void visit(UnaryMinusListNode node) {
		// TODO fill me in
		double operand_one = postfix_stack.pop();
		postfix_stack.push(-(operand_one));
		next_node(node);
	}
}