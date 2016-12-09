package edu.cornell.cs4321.Visitors;

import java.util.HashMap;
import java.util.List;

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
 * This class extracts join expressions from WHERE clause after a traversal of the expression tree.
 * 
 * Algorithm description: 
 * A JoinExpExtractVisitor is aware of the FROM table and JOIN table after constructed.
 * It will traverse the expression, group all expressions into two groups.
 * The group of singleTableExpression only includes expressions related to a single table and pure numerical expressions.
 * The group of joinExpression only includes expressions related to join relations.
 * 
 * @author Chenxi Su cs2238, Hao Qian hq43, Jiangjie Man jm2559
 */
public class JoinExpExtractVisitor implements ExpressionVisitor {
	
	private String fromTable;
	private List<String> joinTables;
	private HashMap<String,Expression> singleTableExpression; //e.g. R.A = 1 AND 42 = 42 AND R.C < 3
	private HashMap<String,Expression> joinExpression; //e.g. String is the name of right table R. 
	
		
	/**
	 * Constructor
	 * @param fromTable there's only one FROM Table obtained from JsqlParser. 
	 * @param joinTables obtained by JoinItems of JsqlParser.
	 */
	public JoinExpExtractVisitor(String fromTable, List<String> joinTables){
		this.fromTable = fromTable;
		this.joinTables = joinTables;
		singleTableExpression = new HashMap<>();
		joinExpression = new HashMap<>();
	}
	
	/**
	 * 
	 * @param ufVisitor
	 */
	//TODO: combine visitor
	//if table.column exits in union find table, use uf table to get table expression
	public void combineUnionFindVisitor(UnionFindVisitor ufVisitor){
		
	}
	
	
	/**
	 * @param tableName a table's name or alias if existed.
	 * @return expression tree on single table, will be used by SelectionOperator.
	 */
	public Expression getSingleTableExpr(String tableName){
		return singleTableExpression.get(tableName);
	}
	
	/**
	 * @param rightTableName the right table's name or alias if existed.
	 * @return expression tree on join relations, will be used by JoinOperator.
	 */
	public Expression getJoinExpr(String rightTableName){
		return joinExpression.get(rightTableName);
	}
	
	/**
	 * Append an expression of a single table to singleTableExpression.
	 * @param expr expression to append
	 * @param tableName about which the expression is
	 */
	private void addSingleTableExpr(Expression expr, String tableName){
		if(singleTableExpression.containsKey(tableName)){
			AndExpression newExpr = new AndExpression();
			newExpr.setLeftExpression(singleTableExpression.get(tableName));
			newExpr.setRightExpression(expr);
			singleTableExpression.put(tableName, newExpr);
		}else{
			singleTableExpression.put(tableName, expr);
		}
	}
	
	/**
	 * Append an expression of two joined tables to joinExpression.
	 * @param expr expression to append
	 * @param tableA table's name
	 * @param tableB the other table's name 
	 */
	private void addJoinExpr(Expression expr, String tableA, String tableB){
		String tableName = (joinTables.indexOf(tableA) > joinTables.indexOf(tableB)) ? tableA : tableB; // the table 
		if (joinExpression.containsKey(tableName)){
			AndExpression newExpr = new AndExpression();
			newExpr.setLeftExpression(joinExpression.get(tableName));
			newExpr.setRightExpression(expr);
			joinExpression.put(tableName, newExpr);
		}else {
			joinExpression.put(tableName, expr);
		}
	}
	
	/**
	 * All binary expressions(comparator expressions) are handled in the same way.
	 * This function determines whether it's a an expression on single table or an expression on join relations.
	 * @param expr a BinaryExpression. In our case, it only includes six types:
	 * ==,!=,>,<,>=,<=
	 */
	private void processCompareExpr(BinaryExpression expr){
		Expression leftExpr = expr.getLeftExpression();
		Expression rightExpr = expr.getRightExpression();
		if((leftExpr instanceof Column) && (rightExpr instanceof Column)){
			Column leftColumn = (Column)leftExpr;
			Column rightColumn = (Column)rightExpr;
			String leftTable = leftColumn.getTable().getName();
			String rightTable = rightColumn.getTable().getName();
			if(!leftTable.equals(rightTable)){
				this.addJoinExpr(expr, leftTable, rightTable);
			}else{
				this.addSingleTableExpr(expr, leftTable);
			}
		}else if(leftExpr instanceof Column){
			this.addSingleTableExpr(expr, ((Column)leftExpr).getTable().getName());
		}else if(rightExpr instanceof Column){
			this.addSingleTableExpr(expr, ((Column)leftExpr).getTable().getName());
		}else{
			this.addSingleTableExpr(expr, this.fromTable); //treat 42 = 42 as part of fromTable's expression
		}
	}

	@Override
	public void visit(NullValue nullValue) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(Function function) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(InverseExpression inverseExpression) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(JdbcParameter jdbcParameter) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(DoubleValue doubleValue) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(LongValue longValue) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(DateValue dateValue) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(TimeValue timeValue) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(TimestampValue timestampValue) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(Parenthesis parenthesis) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(StringValue stringValue) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(Addition addition) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(Division division) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(Multiplication multiplication) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(Subtraction subtraction) {
		// TODO Auto-generated method stub

	}

	/**
	 * For an AndExpression, break it into left expression and right expression, then process them individually.
	 */
	@Override
	public void visit(AndExpression andExpression) {
		andExpression.getLeftExpression().accept(this);
		andExpression.getRightExpression().accept(this);
	}

	@Override
	public void visit(OrExpression orExpression) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(Between between) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(EqualsTo equalsTo) {
		processCompareExpr(equalsTo);
	}

	@Override
	public void visit(GreaterThan greaterThan) {
		processCompareExpr(greaterThan);
	}

	@Override
	public void visit(GreaterThanEquals greaterThanEquals) {
		processCompareExpr(greaterThanEquals);
	}

	@Override
	public void visit(InExpression inExpression) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(IsNullExpression isNullExpression) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(LikeExpression likeExpression) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(MinorThan minorThan) {
		processCompareExpr(minorThan);
	}

	@Override
	public void visit(MinorThanEquals minorThanEquals) {
		processCompareExpr(minorThanEquals);
	}

	@Override
	public void visit(NotEqualsTo notEqualsTo) {
		processCompareExpr(notEqualsTo);
	}

	@Override
	public void visit(Column tableColumn) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(SubSelect subSelect) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(CaseExpression caseExpression) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(WhenClause whenClause) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(ExistsExpression existsExpression) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(AllComparisonExpression allComparisonExpression) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(AnyComparisonExpression anyComparisonExpression) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(Concat concat) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(Matches matches) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(BitwiseAnd bitwiseAnd) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(BitwiseOr bitwiseOr) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(BitwiseXor bitwiseXor) {
		// TODO Auto-generated method stub

	}

}
