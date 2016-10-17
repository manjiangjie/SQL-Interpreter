package edu.cornell.cs4321.LogicalOperators;

import edu.cornell.cs4321.Visitors.PhysicalPlanBuilderVisitor;

/**
 * The logical Distinct operator for building the query plan.
 * @author Jiangjie Man: jm2559
 */
public class LogicalDistinctOperator implements LogicalOperator {
    private LogicalOperator childOperator;

    /**
     * Construct a DISTINCT operator, make it the parent of childOperator in a query plan tree.
     * @param childOperator child operator in the tree of a query plan.
     */
    public LogicalDistinctOperator(LogicalOperator childOperator){
        this.childOperator = childOperator;
    }

    /**
     * Get the child operator of distinct operator.
     * @return the child operator.
     */
    public LogicalOperator getChildOperator() {
        return childOperator;
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
