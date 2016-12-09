package edu.cornell.cs4321.Visitors;

import java.util.ArrayList;
import java.util.List;

import edu.cornell.cs4321.UnionFind.UnionFind;
import net.sf.jsqlparser.expression.AllComparisonExpression;
import net.sf.jsqlparser.expression.AnyComparisonExpression;
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
 * Build a list of union find result
 * @author Heng Kuang hk856
 */
public class UnionFindVisitor implements ExpressionVisitor {
	private List<UnionFind> unionFindList;

	/**
	 * Constructor for union find visitor
	 */
	public UnionFindVisitor() {
		unionFindList = new ArrayList<UnionFind>();
	}

	/**
	 * get union find list
	 * @return union find list
	 */
	public List<UnionFind> getUnionFindList(){
		return unionFindList;
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
	 * If both sides are columns, add two columns to Union Find
	 * If one side is column, update the equality value in Union Find
	 * @param visit equal expression
	 */
	@Override
	public void visit(EqualsTo equalExpression) {
		Expression leftExpr = equalExpression.getLeftExpression();
		Expression rightExpr = equalExpression.getRightExpression();

		// put these table.columns to union find list
		if ((leftExpr instanceof Column) && (rightExpr instanceof Column)) {
			
			Column leftColumn = (Column) leftExpr;
			Column rightColumn = (Column) rightExpr;
			//equal join
			if (!leftColumn.equals(rightColumn)) {
				for(UnionFind u : unionFindList){
					if(!u.checkColumn(leftColumn)&&!u.checkColumn(rightColumn)){
						u.addColumn(leftColumn);
						u.addColumn(rightColumn);
					}else if(u.checkColumn(leftColumn)&&!u.checkColumn(rightColumn)){
						u.addColumn(rightColumn);
					}else if(u.checkColumn(rightColumn)&&!u.checkColumn(leftColumn)){
						u.addColumn(leftColumn);
					}
				}
			} else {
				//equal join itself
			}
		}

		else if (leftExpr instanceof Column) {
			LongValue val = (LongValue) rightExpr;
			for(UnionFind u : unionFindList){
				if(u.checkColumn((Column)leftExpr)){
					u.setEquality(val.getValue());
					break;
				}
			}
		} else if (rightExpr instanceof Column) {
			LongValue val = (LongValue) leftExpr;
			for(UnionFind u : unionFindList){
				if(u.checkColumn((Column)rightExpr)){
					u.setEquality(val.getValue());
					break;
				}
			}
		}
	}

	/**
	 * Update lower bound or upper bound in Union Find
	 * @param greater than expression
	 */
	@Override
	public void visit(GreaterThan exp) {
		Expression leftExpr = exp.getLeftExpression();
		Expression rightExpr = exp.getRightExpression();
		
		if (leftExpr instanceof Column) {
			LongValue val = (LongValue) rightExpr;
			for(UnionFind u : unionFindList){
				if(u.checkColumn((Column)leftExpr) && u.getLowerBound() < 1 + val.getValue()){
					u.setLowerBound(1+val.getValue());
					break;
				}
			}
		} else if (rightExpr instanceof Column) {
			LongValue val = (LongValue) leftExpr;
			for(UnionFind u : unionFindList){
				if(u.checkColumn((Column)rightExpr) && u.getUpperBound() > val.getValue()-1){
					u.setUpperBound(val.getValue()-1);
					break;
				}
			}
		}
	}

	/**
	 * Update lower bound or upper bound in Union Find
	 * @param greater than equals expression
	 */
	@Override
	public void visit(GreaterThanEquals exp) {
		Expression leftExpr = exp.getLeftExpression();
		Expression rightExpr = exp.getRightExpression();
		
		if (leftExpr instanceof Column) {
			LongValue val = (LongValue) rightExpr;
			for(UnionFind u : unionFindList){
				if(u.checkColumn((Column)leftExpr)&& u.getLowerBound() < val.getValue()){
					u.setLowerBound(val.getValue());
					break;
				}
			}
		} else if (rightExpr instanceof Column) {
			LongValue val = (LongValue) leftExpr;
			for(UnionFind u : unionFindList){
				if(u.checkColumn((Column)rightExpr)&& u.getUpperBound() > val.getValue()){
					u.setUpperBound(val.getValue());
					break;
				}
			}
		}
	}
	
	/**
	 * Update lower bound or upper bound in Union Find
	 * @param minor than expression
	 */
	@Override
	public void visit(MinorThan exp) {
		Expression leftExpr = exp.getLeftExpression();
		Expression rightExpr = exp.getRightExpression();
		
		if (leftExpr instanceof Column) {
			LongValue val = (LongValue) rightExpr;
			for(UnionFind u : unionFindList){
				if(u.checkColumn((Column)leftExpr) && u.getUpperBound() > val.getValue() - 1){
					u.setUpperBound(val.getValue()-1);
					break;
				}
			}
		} else if (rightExpr instanceof Column) {
			LongValue val = (LongValue) leftExpr;
			for(UnionFind u : unionFindList){
				if(u.checkColumn((Column)rightExpr) && u.getLowerBound() < val.getValue() + 1){
					u.setLowerBound(val.getValue()+1);
					break;
				}
			}
		}
	}

	/**
	 * Update lower bound or upper bound in Union Find
	 * @param minor than equals expression
	 */
	@Override
	public void visit(MinorThanEquals exp) {
		Expression leftExpr = exp.getLeftExpression();
		Expression rightExpr = exp.getRightExpression();
		
		if (leftExpr instanceof Column) {
			LongValue val = (LongValue) rightExpr;
			for(UnionFind u : unionFindList){
				if(u.checkColumn((Column)leftExpr)&& u.getUpperBound() > val.getValue()){
					u.setUpperBound(val.getValue());
					break;
				}
			}
		} else if (rightExpr instanceof Column) {
			LongValue val = (LongValue) leftExpr;
			for(UnionFind u : unionFindList){
				if(u.checkColumn((Column)rightExpr)&& u.getLowerBound() < val.getValue()){
					u.setLowerBound(val.getValue());
					break;
				}
			}
		}
	}

	//=====================================================
	
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
