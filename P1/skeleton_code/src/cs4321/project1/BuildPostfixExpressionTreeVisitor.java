package cs4321.project1;

import cs4321.project1.list.*;
import cs4321.project1.tree.*;

/**
 * Perform a post-order traversal and build a list based off of the expression
 *
 * @author Albert Leung; al2237
 */
public class BuildPostfixExpressionTreeVisitor implements TreeVisitor {

	private ListNode head;
	private ListNode tail;

	private void add_node_at_end(ListNode node){
		if(head == null && tail == null){
			head = tail = node;
		}
		else{
			tail.setNext(node);
			tail = node;
		}
	}

	public BuildPostfixExpressionTreeVisitor() {
		// TODO fill me in
		head = tail = null;
	}

	public ListNode getResult() {
		// TODO fill me in
		return head;
	}

	@Override
	public void visit(LeafTreeNode node) {
		// TODO fill me in
		NumberListNode single_node = new NumberListNode(node.getData());
		add_node_at_end(single_node);
	}

	@Override
	public void visit(UnaryMinusTreeNode node) {
		// TODO fill me in
		node.getChild().accept(this);
		UnaryMinusListNode unary_minus_operator = new UnaryMinusListNode();
		add_node_at_end(unary_minus_operator);
	}

	@Override
	public void visit(AdditionTreeNode node) {
		// TODO fill me in
		node.getLeftChild().accept(this);
		node.getRightChild().accept(this);
		AdditionListNode addition_operator = new AdditionListNode();
		add_node_at_end(addition_operator);
	}

	@Override
	public void visit(MultiplicationTreeNode node) {
		// TODO fill me in
		node.getLeftChild().accept(this);
		node.getRightChild().accept(this);
		MultiplicationListNode multiplication_operator = new MultiplicationListNode();
		add_node_at_end(multiplication_operator);
	}

	@Override
	public void visit(SubtractionTreeNode node) {
		// TODO fill me in
		node.getLeftChild().accept(this);
		node.getRightChild().accept(this);
		SubtractionListNode subtraction_operator = new SubtractionListNode();
		add_node_at_end(subtraction_operator);
	}

	@Override
	public void visit(DivisionTreeNode node) {
		// TODO fill me in
		node.getLeftChild().accept(this);
		node.getRightChild().accept(this);
		DivisionListNode division_operator = new DivisionListNode();
		add_node_at_end(division_operator);
	}
}