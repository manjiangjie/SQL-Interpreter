package edu.cornell.cs4321.LogicalOperators;

import edu.cornell.cs4321.Visitors.PhysicalPlanBuilderVisitor;
import net.sf.jsqlparser.expression.Expression;

/**
 * The logical join operator for building the query plan.
 * @author Jiangjie Man: jm2559
 */
public class LogicalJoinOperator implements LogicalOperator {
    private LogicalOperator leftChildOperator;
    private LogicalOperator rightChildOperator;
    private Expression joinExpression;

    /**
     * Constructor for the logical join operator.
     * @param leftChildOperator the left child operator.
     * @param rightChildOperator the right child operator.
     * @param joinExpression join condition.
     */
    public LogicalJoinOperator(LogicalOperator leftChildOperator, LogicalOperator rightChildOperator, Expression joinExpression) {
        this.leftChildOperator = leftChildOperator;
        this.rightChildOperator = rightChildOperator;
        this.joinExpression = joinExpression;
    }

    /**
     * Get the left child operator.
     * @return the left child operator.
     */
    public LogicalOperator getLeftChildOperator() {
        return leftChildOperator;
    }

    /**
     * Get the right child operator.
     * @return the right child operator.
     */
    public LogicalOperator getRightChildOperator() {
        return rightChildOperator;
    }

    /**
     * Get the join condition.
     * @return the expression of join condition.
     */
    public Expression getJoinExpression() {
        return joinExpression;
    }

    /**
     * Accept method for PhysicalPlanBuilderVisitor.
     * @param v A visitor.
     */
    @Override
    public void accept(PhysicalPlanBuilderVisitor v) {
        v.visit(this);
    }
}
