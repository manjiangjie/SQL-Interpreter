package edu.cornell.cs4321.LogicalOperators;

import edu.cornell.cs4321.Visitors.PhysicalPlanBuilderVisitor;

/**
 * The logical operator for scan operations.
 * @author Jiangjie Man: jm2559
 */
public class LogicalScanOperator implements LogicalOperator {
    private String tableName;
    private String alias;

    /**
     * Constructor for logical scan operator.
     * @param tableName the table to be scanned
     */
    public LogicalScanOperator(String tableName, String alias) {
        this.tableName = tableName;
        this.alias = alias;
    }

    /**
     * Get the table name.
     * @return the table to be scanned
     */
    public String getTableName() {
        return tableName;
    }

    /**
     * Get the alias of the table.
     * @return the alias of the table
     */
    public String getAlias() {
        return alias;
    }

    /**
     * Accept method for PhysicalPlanBuilderVisitor.
     * @param v A visitor
     */
    @Override
    public void accept(PhysicalPlanBuilderVisitor v) {
        v.visit(this);
    }
}
