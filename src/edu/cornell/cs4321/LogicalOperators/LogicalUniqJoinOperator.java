package edu.cornell.cs4321.LogicalOperators;

import java.util.ArrayList;
import java.util.List;

import edu.cornell.cs4321.Visitors.PhysicalPlanBuilderVisitor;
import net.sf.jsqlparser.expression.Expression;

/**
 * The logical join operator for building the query plan.
 * @author Jiangjie Man: jm2559
 */
public class LogicalUniqJoinOperator implements LogicalOperator {
    private List<LogicalOperator> ChildrenOperators;
    private List<Expression> residualExpressions;

    /**
     * Constructor for the logical join operator.
     * @param firstChildOperator the first child operator.
     */
    //TODO Make this more than two children
    public LogicalUniqJoinOperator() {
    	ChildrenOperators = new ArrayList<LogicalOperator>();
    }
    
    /**
     * Constructor for the logical join operator.
     * @param firstChildOperator the first child operator.
     */
    //TODO Make this more than two children
    public LogicalUniqJoinOperator(List<Expression> residualExpressions) {
    	ChildrenOperators = new ArrayList<LogicalOperator>();
    	this.residualExpressions = residualExpressions;
    }
    
    /**
     * add a child operator to the join operator
     * @param operator
     * @param expression
     */
    public void addOperator(LogicalOperator operator){
    	ChildrenOperators.add(operator);
    }

    /**
     * Get all children operators.
     * @return all children operators.
     */
    public List<LogicalOperator> ChildrenOperators() {
        return ChildrenOperators;
    }

    /**
     * Get the residual expressions that can't be put into union-find.
     * @return the list of residual expressions.
     */
    public List<Expression> getResidualExpression() {
        return residualExpressions;
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
