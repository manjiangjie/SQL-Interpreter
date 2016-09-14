package cs4321.project1;

import cs4321.project1.tree.*;

/**
 * Class for a parser that can parse a string and produce an expression tree. To
 * keep the code simple, this does no input checking whatsoever so it only works
 * on correct input.
 * 
 * An expression is one or more terms separated by + or - signs. A term is one
 * or more factors separated by * or / signs. A factor is an expression in
 * parentheses (), a factor with a unary - before it, or a number.
 * 
 * @author Lucja Kot
 * @author Jiangjie Man; jm2559
 */
public class Parser {

	private String[] tokens;
	private int currentToken; // pointer to next input token to be processed

	/**
	 * @precondition input represents a valid expression with all tokens
	 *               separated by spaces, e.g. "3.0 - ( 1.0 + 2.0 ) / - 5.0. All
	 *               tokens must be either numbers that parse to Double, or one
	 *               of the symbols +, -, /, *, ( or ), and all parentheses must
	 *               be matched and properly nested.
	 */
	public Parser(String input) {
		this.tokens = input.split("\\s+");
		currentToken = 0;
	}

	/**
	 * Parse the input and build the expression tree
	 * 
	 * @return the (root node of) the resulting tree
	 */
	public TreeNode parse() {
		return expression();
	}

	/**
	 * Parse the remaining input as far as needed to get the next factor
	 * 
	 * @return the (root node of) the resulting subtree
	 */
	private TreeNode factor() {
		TreeNode result;
		if (tokens[currentToken].equals("(")) {
			currentToken += 1;
			result = expression();
		} else if (tokens[currentToken].equals("-")) {
			currentToken += 1;
			result = new UnaryMinusTreeNode(factor());
		} else {
			result = new LeafTreeNode(Double.parseDouble(tokens[currentToken]));
			currentToken += 1;
		}
		return result;
	}

	/**
	 * Parse the remaining input as far as needed to get the next term
	 * 
	 * @return the (root node of) the resulting subtree
	 */
	private TreeNode term() {
		TreeNode result = factor();
		while (currentToken < tokens.length && (tokens[currentToken].equals("*") || tokens[currentToken].equals("/"))) {
			if (tokens[currentToken].equals("*")) {
				currentToken += 1;
				result = new MultiplicationTreeNode(result, factor());
			} else if (tokens[currentToken].equals("/")) {
				currentToken += 1;
				result = new DivisionTreeNode(result, factor());
			}
		}
		if (currentToken < tokens.length - 1 && tokens[currentToken].equals(")")) {
			currentToken += 1;
		}
		return result;
	}

	/**
	 * Parse the remaining input as far as needed to get the next expression
	 * 
	 * @return the (root node of) the resulting subtree
	 */
	private TreeNode expression() {
		TreeNode result = term();
		while (currentToken < tokens.length && (tokens[currentToken].equals("+") || tokens[currentToken].equals("-"))) {
			if (tokens[currentToken].equals("+")) {
				currentToken += 1;
				result = new AdditionTreeNode(result, term());
			} else if (tokens[currentToken].equals("-")) {
				currentToken += 1;
				result = new SubtractionTreeNode(result, term());
			}
		}
		if (currentToken < tokens.length - 1 &&tokens[currentToken].equals(")")) {
			currentToken += 1;
		}
		return result;
	}
}
