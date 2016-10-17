package edu.cornell.cs4321.LogicalOperators;

import edu.cornell.cs4321.Visitors.PhysicalPlanBuilderVisitor;
import net.sf.jsqlparser.expression.Expression;

/**
 * The logical selection operator for the query plan.
 * @author Jiangjie Man: jm2559
 */
public class LogicalSelectionOperator implements LogicalOperator {
    private Expression selectionCondition; //the root of the selection condition for this table
    private LogicalOperator childOperator; //Note: it would be the best to just give it a type as Operator instead of a specific one

    /**
     * Constructor
     * @param childOperator child operator in a query plan(tree)
     * @param expr A Expression which represents of selection condition of SelectionOperator
     * */
    public LogicalSelectionOperator(LogicalOperator childOperator, Expression expr) {
        this.selectionCondition = expr;
        this.childOperator = childOperator;
    }

    /**
     * get the selection condition of selectionOperator
     * @return Expression selection expression
     * */
    public Expression getSelectionCondition(){
        return selectionCondition;
    }

    /**
     * getter method
     * @return child operator
     * */
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
