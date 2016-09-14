package cs4321.project1;

import cs4321.project1.tree.DivisionTreeNode;
import cs4321.project1.tree.LeafTreeNode;
import cs4321.project1.tree.SubtractionTreeNode;
import cs4321.project1.tree.AdditionTreeNode;
import cs4321.project1.tree.MultiplicationTreeNode;
import cs4321.project1.tree.UnaryMinusTreeNode;

import java.util.Stack;

/**
 * Evaluates the tree to a single number
 * 
 * @author Jiangjie Man; jm2559
 */

public class EvaluateTreeVisitor implements TreeVisitor {

	private double result;
	private Stack<Double> st;

	public EvaluateTreeVisitor() {
		st = new Stack<>();
	}

	/**
	 * Method to get the evaluation number
	 *
	 * @return the evaluation number of string representation of the visited tree
	 */
	public double getResult() {
		return result; // so that skeleton code compiles
	}

	/**
	 * Visit method for leaf node; push the node to stack
	 *
	 * @param node
	 *            the node to be visited
	 */
	@Override
	public void visit(LeafTreeNode node) {
		result = node.getData();
		st.push(result);
	}

	/**
	 * Visit method for unary minus node; recursively visit subtree and pop the negative value off the stack
	 *
	 * @param node
	 *            the node to be visited
	 */
	@Override
	public void visit(UnaryMinusTreeNode node) {
		node.getChild().accept(this);
		result = -st.pop();
		st.push(result);
	}

	/**
	 * Visit method for addition node based on inorder tree traversal
	 *
	 * @param node
	 *            the node to be visited
	 */
	@Override
	public void visit(AdditionTreeNode node) {
		node.getLeftChild().accept(this);
		node.getRightChild().accept(this);
		result = st.pop() + st.pop();
		st.push(result);
	}

	/**
	 * Visit method for multiplication node based on inorder tree traversal
	 *
	 * @param node
	 *            the node to be visited
	 */
	@Override
	public void visit(MultiplicationTreeNode node) {
		node.getLeftChild().accept(this);
		node.getRightChild().accept(this);
		result = st.pop() * st.pop();
		st.push(result);
	}

	/**
	 * Visit method for subtraction node based on inorder tree traversal
	 *
	 * @param node
	 *            the node to be visited
	 */
	@Override
	public void visit(SubtractionTreeNode node) {
		node.getLeftChild().accept(this);
		node.getRightChild().accept(this);
		double a = st.pop();
		double b = st.pop();
		result = b - a;
		st.push(result);
	}

	/**
	 * Visit method for division node based on inorder tree traversal
	 *
	 * @param node
	 *            the node to be visited
	 */
	@Override
	public void visit(DivisionTreeNode node) {
		node.getLeftChild().accept(this);
		node.getRightChild().accept(this);
		double a = st.pop();
		double b = st.pop();
		result = b / a;
		st.push(result);
	}
}
