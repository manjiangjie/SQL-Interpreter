package edu.cornell.cs4321.Visitors;

import edu.cornell.cs4321.Database.Tuple;
import net.sf.jsqlparser.expression.*;
import net.sf.jsqlparser.expression.operators.arithmetic.*;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.relational.*;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.SubSelect;

/**
 * This visitor can traverse an expression and evaluate it on a specified tuple.
 *  @author Chenxi Su cs2238, Hao Qian hq43, Jiangjie Man jm2559
 */
public class WhereExpressionVisitor implements ExpressionVisitor {
    private long numValue; //store a numerical value for evaluation later.
    private boolean evalResult; //final evaluation result.
    private Tuple tuple;

    public WhereExpressionVisitor(Tuple t) {
        tuple = t;
    }

    /**
     * getter method
     * @return expression's evaluation result
     */
    public boolean getResult() {
        return evalResult;
    }

    /**
     * getter method
     * @return expression's numerical value
     */
    private long getValue() {
        return numValue;
    }

    /**
     * getter method
     * @return a tuple on which the expression is evaluated.
     */
    public Tuple getTuple() {
        return tuple;
    }

    /**
     * Visit andExpression
     */
    @Override
    public void visit(AndExpression andExpression) {
        andExpression.getLeftExpression().accept(this);
        boolean leftResult = this.getResult();
        andExpression.getRightExpression().accept(this);
        boolean rightResult = this.getResult();
        evalResult = (leftResult && rightResult);
    }

    /**
     * set numerical value
     */
    @Override
    public void visit(LongValue longValue) {
        numValue = longValue.getValue();
    }

    /**
     * Visit a Column, return it's value by looking it up in tuple.
     */
    @Override
    public void visit(Column column) {
        numValue = tuple.getValueByCol(column).longValue();
    }

    /**
     * Evaluate xx = yy expression
     */
    @Override
    public void visit(EqualsTo equalsTo) {
        equalsTo.getLeftExpression().accept(this);
        long leftVal = this.getValue();
        equalsTo.getRightExpression().accept(this);
        long rightVal = this.getValue();
        evalResult = (leftVal == rightVal);
    }

    /**
     * Evaluate xx != yy expression
     */
    @Override
    public void visit(NotEqualsTo notEqualsTo) {
    	notEqualsTo.getLeftExpression().accept(this);
        long leftVal = this.getValue();
        notEqualsTo.getRightExpression().accept(this);
        long rightVal = this.getValue();
        evalResult = (leftVal != rightVal);
    }

    /**
     * Evaluate xx > yy expression
     */
    @Override
    public void visit(GreaterThan greaterThan) {
    	greaterThan.getLeftExpression().accept(this);
        long leftVal = this.getValue();
        greaterThan.getRightExpression().accept(this);
        long rightVal = this.getValue();
        evalResult = (leftVal > rightVal);
    }

    /**
     * Evaluate xx >= yy expression
     */
    @Override
    public void visit(GreaterThanEquals greaterThanEquals) {
    	greaterThanEquals.getLeftExpression().accept(this);
        long leftVal = this.getValue();
        greaterThanEquals.getRightExpression().accept(this);
        long rightVal = this.getValue();
        evalResult = (leftVal >= rightVal);
    }

    /**
     * Evaluate xx < yy expression
     */
    @Override
    public void visit(MinorThan minorThan) {
    	minorThan.getLeftExpression().accept(this);
        long leftVal = this.getValue();
        minorThan.getRightExpression().accept(this);
        long rightVal = this.getValue();
        evalResult = (leftVal < rightVal);
    }

    /**
     * Evaluate xx <= yy expression
     */
    @Override
    public void visit(MinorThanEquals minorThanEquals) {
    	minorThanEquals.getLeftExpression().accept(this);
        long leftVal = this.getValue();
        minorThanEquals.getRightExpression().accept(this);
        long rightVal = this.getValue();
        evalResult = (leftVal <= rightVal);
    }

    @Override
    public void visit(NullValue nullValue) {

    }

    @Override
    public void visit(Function function) {

    }

    @Override
    public void visit(InverseExpression inverseExpression) {

    }

    @Override
    public void visit(JdbcParameter jdbcParameter) {

    }

    @Override
    public void visit(DoubleValue doubleValue) {

    }

    @Override
    public void visit(DateValue dateValue) {

    }

    @Override
    public void visit(TimeValue timeValue) {

    }

    @Override
    public void visit(TimestampValue timestampValue) {

    }

    @Override
    public void visit(Parenthesis parenthesis) {

    }

    @Override
    public void visit(StringValue stringValue) {

    }

    @Override
    public void visit(Addition addition) {

    }

    @Override
    public void visit(Division division) {

    }

    @Override
    public void visit(Multiplication multiplication) {

    }

    @Override
    public void visit(Subtraction subtraction) {

    }

    @Override
    public void visit(OrExpression orExpression) {

    }

    @Override
    public void visit(Between between) {

    }

    @Override
    public void visit(InExpression inExpression) {

    }

    @Override
    public void visit(IsNullExpression isNullExpression) {

    }

    @Override
    public void visit(LikeExpression likeExpression) {

    }

    @Override
    public void visit(SubSelect subSelect) {

    }

    @Override
    public void visit(CaseExpression caseExpression) {

    }

    @Override
    public void visit(WhenClause whenClause) {

    }

    @Override
    public void visit(ExistsExpression existsExpression) {

    }

    @Override
    public void visit(AllComparisonExpression allComparisonExpression) {

    }

    @Override
    public void visit(AnyComparisonExpression anyComparisonExpression) {

    }

    @Override
    public void visit(Concat concat) {

    }

    @Override
    public void visit(Matches matches) {

    }

    @Override
    public void visit(BitwiseAnd bitwiseAnd) {

    }

    @Override
    public void visit(BitwiseOr bitwiseOr) {

    }

    @Override
    public void visit(BitwiseXor bitwiseXor) {

    }
}
