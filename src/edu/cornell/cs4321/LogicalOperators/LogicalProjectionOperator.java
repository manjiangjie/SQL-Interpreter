package edu.cornell.cs4321.LogicalOperators;

import edu.cornell.cs4321.Visitors.PhysicalPlanBuilderVisitor;

/**
 * The logical projection operator for query plan.
 * @author Jiangjie Man: jm2559
 */
public class LogicalProjectionOperator implements LogicalOperator {
    private LogicalOperator childOperator;

    /**
     * Construct ProjectionOperator with child operator and a list attributes to project.
     */
    public LogicalProjectionOperator(LogicalOperator op){
        childOperator = op;
    }

    /**
     * Get the child operator of this class.
     * @return the child operator
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
