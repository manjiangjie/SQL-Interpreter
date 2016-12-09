package edu.cornell.cs4321.LogicalOperators;

import java.util.List;

import edu.cornell.cs4321.Visitors.PhysicalPlanBuilderVisitor;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.SelectItem;

/**
 * The logical projection operator for query plan.
 * @author Jiangjie Man: jm2559
 */
public class LogicalProjectionOperator implements LogicalOperator {
    private LogicalOperator childOperator;
    private List<SelectItem> projectList;
    
    /**
     * Construct ProjectionOperator with child operator and a list attributes to project.
     */
    public LogicalProjectionOperator(LogicalOperator op, List<SelectItem> pl){
        childOperator = op;
        projectList = pl;
    }

    /**
     * Get the child operator of this class.
     * @return the child operator
     */
    public LogicalOperator getChildOperator() {
        return childOperator;
    }
    
    /**
     * Get the projection column list.
     * @return the list of project columns
     */
    public List<SelectItem> getProjectList() {
        return projectList;
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
