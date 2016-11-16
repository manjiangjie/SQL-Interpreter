package edu.cornell.cs4321.Visitors;

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
 * IndexExpExtractVisitor can visit a selectionExpression, divide the expression
 * into lwokey/highkey part which indexes help and the part that indexes can't help.
 * 
 */
public class IndexExpExtractVisitor implements ExpressionVisitor {
	
	private String indexedColName;
	private Long lowkey;
	private Boolean lowOpen;
	private Long highkey;
	private Boolean highOpen;
	private Expression exprWithoutIndex;
	
	public IndexExpExtractVisitor(String indexedColName) {
		this.indexedColName = indexedColName;
	}
	
	public Long getLowkey() {
		return lowkey;
	}

	public Boolean isLowOpen() {
		return lowOpen;
	}

	public Long getHighkey() {
		return highkey;
	}

	public Boolean isHighOpen() {
		return highOpen;
	}

	public Expression getExprWithoutIndex() {
		return exprWithoutIndex;
	}

	private boolean hasNonIndexedColumn(BinaryExpression expr) {
		Expression leftChild = expr.getLeftExpression();
		Expression rightChild = expr.getRightExpression();
		if(leftChild instanceof Column) {
			if(!((Column)leftChild).getColumnName().equals(this.indexedColName)){
				return true;
			}
		}
		if(rightChild instanceof Column) {
			if(!((Column)rightChild).getColumnName().equals(this.indexedColName)){
				return true;
			}
		}
		return false;
	}
	
	private void addExprWithoutIndex(Expression expr) {
		if(this.exprWithoutIndex == null) {
			this.exprWithoutIndex = expr;
		} else {
			this.exprWithoutIndex = new AndExpression(this.exprWithoutIndex, expr);
		}
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
	public void visit(AndExpression arg0) {
		arg0.getLeftExpression().accept(this);
		arg0.getRightExpression().accept(this);
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
	public void visit(EqualsTo arg0) {
		Expression leftChild = arg0.getLeftExpression();
		Expression rightChild = arg0.getRightExpression();
		
		// If an expression contains a non-indexed column, then add this expression to exprWithoutIndex
		if(this.hasNonIndexedColumn(arg0)){
			this.addExprWithoutIndex(arg0);
			return;
		} else {
			if(leftChild instanceof LongValue) {  // 3 = R.A
				this.lowkey = ((LongValue)leftChild).getValue();
				this.highkey = this.lowkey;
				this.lowOpen = false;
				this.highOpen = this.lowOpen;
			} else if(rightChild instanceof LongValue) { // R.A = 3
				this.lowkey = ((LongValue)rightChild).getValue();
				this.highkey = this.lowkey;
				this.lowOpen = false;
				this.highOpen = this.lowOpen;
			}
		}		
	}

	@Override
	public void visit(GreaterThan arg0) {
		Expression leftChild = arg0.getLeftExpression();
		Expression rightChild = arg0.getRightExpression();
		
		// If an expression contains a non-indexed column, then add this expression to exprWithoutIndex
		if(this.hasNonIndexedColumn(arg0)){
			this.addExprWithoutIndex(arg0);
			return;
		} else {
			if(leftChild instanceof LongValue) {  // 3 > R.A
				long val = ((LongValue)leftChild).getValue();
				if(this.highkey == null || this.highkey >= val){
					this.highkey = val;
					this.highOpen = true;
				}
			} else if(rightChild instanceof LongValue) { // R.A > 3
				long val = ((LongValue)rightChild).getValue();
				if(this.lowkey == null || this.lowkey <= val) {
					this.lowkey = val;
					this.lowOpen = true;
				}
			}
		}
	}

	@Override
	public void visit(GreaterThanEquals arg0) {
		Expression leftChild = arg0.getLeftExpression();
		Expression rightChild = arg0.getRightExpression();
		
		// If an expression contains a non-indexed column, then add this expression to exprWithoutIndex
		if(this.hasNonIndexedColumn(arg0)){
			this.addExprWithoutIndex(arg0);
			return;
		} else {
			if(leftChild instanceof LongValue) {  // 3 >= R.A
				long val = ((LongValue)leftChild).getValue();
				if(this.highkey == null || this.highkey > val){
					this.highkey = val;
					this.highOpen = false;
				}
			} else if(rightChild instanceof LongValue) { // R.A >= 3
				long val = ((LongValue)rightChild).getValue();
				if(this.lowkey == null || this.lowkey < val){
					this.lowkey = val;
					this.lowOpen = false;
				}
			}
		}
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
	public void visit(MinorThan arg0) {
		Expression leftChild = arg0.getLeftExpression();
		Expression rightChild = arg0.getRightExpression();
		
		// If an expression contains a non-indexed column, then add this expression to exprWithoutIndex
		if(this.hasNonIndexedColumn(arg0)){
			this.addExprWithoutIndex(arg0);
			return;
		} else {
			if(leftChild instanceof LongValue) {  // 3 < R.A
				long val = ((LongValue)leftChild).getValue();
				if(this.lowkey == null || this.lowkey <= val){
					this.lowkey = val;
					this.lowOpen = true;
				}
			} else if(rightChild instanceof LongValue) { // R.A < 3
				long val = ((LongValue)rightChild).getValue();
				if(this.highkey == null || this.highkey >= val){
					this.highkey = val;
					this.highOpen = true;
				}
			}
		}
	}

	@Override
	public void visit(MinorThanEquals arg0) {
		Expression leftChild = arg0.getLeftExpression();
		Expression rightChild = arg0.getRightExpression();
		
		// If an expression contains a non-indexed column, then add this expression to exprWithoutIndex
		if(this.hasNonIndexedColumn(arg0)){
			this.addExprWithoutIndex(arg0);
			return;
		} else {
			if(leftChild instanceof LongValue) {  // 3 <= R.A
				long val = ((LongValue)leftChild).getValue();
				if(this.lowkey == null || this.lowkey < val){
					this.lowkey = val;
					this.lowOpen = false;
				}
			} else if(rightChild instanceof LongValue) { // R.A <= 3
				long val = ((LongValue)rightChild).getValue();
				if(this.highkey == null || this.highkey > val){
					this.highkey = val;
					this.highOpen = false;
				}
			}
		}
	}

	@Override
	public void visit(NotEqualsTo arg0) {
		this.addExprWithoutIndex(arg0);
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
