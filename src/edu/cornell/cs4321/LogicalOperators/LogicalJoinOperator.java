package edu.cornell.cs4321.LogicalOperators;

import java.util.ArrayList;
import java.util.List;

import edu.cornell.cs4321.Visitors.PhysicalPlanBuilderVisitor;
import net.sf.jsqlparser.expression.Expression;

/**
 * The logical join operator for building the query plan.
 * @author Jiangjie Man: jm2559
 */
public class LogicalJoinOperator implements LogicalOperator {
    private List<LogicalOperator> ChildrenOperators;
    private List<Expression> joinExpressions;

    /**
     * Constructor for the logical join operator.
     * @param firstChildOperator the first child operator.
     */
    //TODO Make this more than two children
    public LogicalJoinOperator(LogicalOperator firstChildOperator) {
    	ChildrenOperators = new ArrayList<LogicalOperator>();
    	ChildrenOperators.add(firstChildOperator);
    	joinExpressions = new ArrayList<Expression>();
    }
    
    /**
     * add a child operator to the join operator
     * @param operator
     * @param expression
     */
    public void addOperator(LogicalOperator operator, Expression expression){
    	ChildrenOperators.add(operator);
    	joinExpressions.add(expression);
    }

    /**
     * Get all children operators.
     * @return all children operators.
     */
    public List<LogicalOperator> ChildrenOperators() {
        return ChildrenOperators;
    }

    /**
     * Get the join condition.
     * @return the expression of join condition.
     */
    public List<Expression> getJoinExpression() {
        return joinExpressions;
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
