package edu.cornell.cs4321.LogicalOperators;

import edu.cornell.cs4321.Visitors.PhysicalPlanBuilderVisitor;

/**
 * This class is the interface of different logical operators.
 * @author Jiangjie Man: jm2559
 */
public interface LogicalOperator {

    void accept(PhysicalPlanBuilderVisitor v);
}
