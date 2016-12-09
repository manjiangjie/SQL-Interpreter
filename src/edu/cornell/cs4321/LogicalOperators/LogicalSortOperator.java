package edu.cornell.cs4321.LogicalOperators;

import edu.cornell.cs4321.Visitors.PhysicalPlanBuilderVisitor;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.OrderByElement;

import java.util.LinkedList;
import java.util.List;

/**
 * The logical sort operator for building the query plan.
 * @author Jiangjie Man: jm2559
 */
public class LogicalSortOperator implements LogicalOperator {
    private LogicalOperator childOperator;
    private List<Column> sortByColumns;

    public LogicalSortOperator(LogicalOperator childOperator, List<Column> orderByList) {
        this.childOperator = childOperator;
        if (orderByList != null) {
            List<Column> sortByCols = new LinkedList<>();
            if (orderByList != null) {
                sortByColumns = orderByList;
            } else {
            	this.sortByColumns = sortByCols;
            }
        } else {
            this.sortByColumns = new LinkedList<>();
        }
    }

    /**
     * Get the child operator of sort operator.
     * @return the child operator.
     */
    public LogicalOperator getChildOperator() {
        return childOperator;
    }

    /**
     * Get the sorted columns.
     * @return the columns to be sorted by.
     */
    public List<Column> getSortByColumns() {
        return sortByColumns;
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
