package edu.cornell.cs4321.Visitors;

import java.util.List;

import edu.cornell.cs4321.UnionFind.UnionFind;
import net.sf.jsqlparser.expression.AllComparisonExpression;
import net.sf.jsqlparser.expression.AnyComparisonExpression;
import net.sf.jsqlparser.expression.BinaryExpression;
import net.sf.jsqlparser.expression.CaseExpression;
import net.sf.jsqlparser.expression.DateValue;
import net.sf.jsqlparser.expression.DoubleValue;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.ExpressionVisitor;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.expression.InverseExpression;
import net.sf.jsqlparser.expression.JdbcParameter;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.NullValue;
import net.sf.jsqlparser.expression.Parenthesis;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.TimeValue;
import net.sf.jsqlparser.expression.TimestampValue;
import net.sf.jsqlparser.expression.WhenClause;
import net.sf.jsqlparser.expression.operators.arithmetic.Addition;
import net.sf.jsqlparser.expression.operators.arithmetic.BitwiseAnd;
import net.sf.jsqlparser.expression.operators.arithmetic.BitwiseOr;
import net.sf.jsqlparser.expression.operators.arithmetic.BitwiseXor;
import net.sf.jsqlparser.expression.operators.arithmetic.Concat;
import net.sf.jsqlparser.expression.operators.arithmetic.Division;
import net.sf.jsqlparser.expression.operators.arithmetic.Multiplication;
import net.sf.jsqlparser.expression.operators.arithmetic.Subtraction;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.relational.Between;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.ExistsExpression;
import net.sf.jsqlparser.expression.operators.relational.GreaterThan;
import net.sf.jsqlparser.expression.operators.relational.GreaterThanEquals;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import net.sf.jsqlparser.expression.operators.relational.IsNullExpression;
import net.sf.jsqlparser.expression.operators.relational.LikeExpression;
import net.sf.jsqlparser.expression.operators.relational.Matches;
import net.sf.jsqlparser.expression.operators.relational.MinorThan;
import net.sf.jsqlparser.expression.operators.relational.MinorThanEquals;
import net.sf.jsqlparser.expression.operators.relational.NotEqualsTo;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.SubSelect;

/**
 * construct a pushed Expression tree based on 
 * query select expression and union find
 * @author Heng Kuang hk856
 *
 */
public class PushSelectVisitor implements ExpressionVisitor{
	private List<UnionFind> unionFindList;
	private AndExpression newExpression;
	private String tableName;
	
	/**
	 * Constructor for push select visitor
	 * @param unionFindList
	 */
	public PushSelectVisitor(List<UnionFind> unionFindList, String tableName){
		this.unionFindList = unionFindList;
		this.tableName = tableName;
		newExpression = new AndExpression();
		
		for(UnionFind u : unionFindList){
			BinaryExpression exprTree = u.buildExpressionTree(tableName);
			if(exprTree!=null&&exprTree.getRightExpression()!=null){
				addToExpression(exprTree);
			}else if(exprTree!=null&&exprTree.getRightExpression()==null){
				addToExpression(exprTree.getLeftExpression());
			}
		}
	}
	
	
	public Expression getExpression(){
		return newExpression.getRightExpression()==null? newExpression.getLeftExpression(): newExpression;
	}
	
	/**
	 * add the input expression to output expression
	 * @param expression
	 */
	private void addToExpression(Expression e){
		if(newExpression.getLeftExpression()==null)
			newExpression.setLeftExpression(e); 
		else if(newExpression.getRightExpression()==null)
			newExpression.setRightExpression(e);
		else
			newExpression = new AndExpression(newExpression, e);
	}
	
	/**
	 * For an AndExpression, break it into left expression and right expression,
	 * then process them individually.
	 */
	@Override
	public void visit(AndExpression andExpression) {
		andExpression.getLeftExpression().accept(this);
		andExpression.getRightExpression().accept(this);
	}
	
	/**
	 * build output expression excluding columns in union find
	 * @param expression
	 */
	private void buildOutputExpression(BinaryExpression expr){
		Expression leftExpr = expr.getLeftExpression();
		Expression rightExpr = expr.getRightExpression();

		if (leftExpr instanceof Column) {
			for(UnionFind u : unionFindList){
				if(u.checkColumn((Column)leftExpr)){
					return;
				}
			}
			addToExpression(expr);
		} 
		
		if (rightExpr instanceof Column) {
			for(UnionFind u : unionFindList){
				if(u.checkColumn((Column)rightExpr)){
					return;
				}
			}
			addToExpression(expr);
		}
	}
	
	/**
	 * @param BinaryExpression
	 * if column is not in union find, add it to output expression tree
	 */
	@Override
	public void visit(EqualsTo equalExpression) {
		buildOutputExpression(equalExpression);
	}

	/**
	 * @param BinaryExpression
	 * if column is not in union find, add it to output expression tree
	 */
	@Override
	public void visit(GreaterThan exp) {
		buildOutputExpression(exp);
	}

	/**
	 * @param BinaryExpression
	 * if column is not in union find, add it to output expression tree
	 */
	@Override
	public void visit(GreaterThanEquals exp) {
		buildOutputExpression(exp);
	}
	
	/**
	 * @param BinaryExpression
	 * if column is not in union find, add it to output expression tree
	 */
	@Override
	public void visit(MinorThan exp) {
		buildOutputExpression(exp);
	}

	/**
	 * @param BinaryExpression
	 * if column is not in union find, add it to output expression tree
	 */
	@Override
	public void visit(MinorThanEquals exp) {
		buildOutputExpression(exp);
	}

	
	
	@Override
	public void visit(NullValue arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(Function arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(InverseExpression arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(JdbcParameter arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(DoubleValue arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(LongValue arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(DateValue arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(TimeValue arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(TimestampValue arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(Parenthesis arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(StringValue arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(Addition arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(Division arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(Multiplication arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(Subtraction arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(OrExpression arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(Between arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(InExpression arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(IsNullExpression arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(LikeExpression arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(NotEqualsTo arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(Column arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(SubSelect arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(CaseExpression arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(WhenClause arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(ExistsExpression arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(AllComparisonExpression arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(AnyComparisonExpression arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(Concat arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(Matches arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(BitwiseAnd arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(BitwiseOr arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(BitwiseXor arg0) {
		// TODO Auto-generated method stub
		
	}

}
