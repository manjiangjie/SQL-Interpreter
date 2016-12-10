package edu.cornell.cs4321;

import edu.cornell.cs4321.Database.DatabaseCatalog;
import edu.cornell.cs4321.LogicalOperators.LogicalOperator;
import edu.cornell.cs4321.LogicalOperators.LogicalScanOperator;
import edu.cornell.cs4321.LogicalOperators.LogicalSelectionOperator;
import edu.cornell.cs4321.LogicalOperators.LogicalUniqJoinOperator;
import edu.cornell.cs4321.UnionFind.Element;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.schema.Column;

import java.util.*;

/**
 * The logical join operator with arbitrarily many children, and needs to translate this into a “real” left-deep join tree
 * by choosing join orders and implementations for each join.
 *
 * @author Jiangjie Man: jm2559
 */
public class JoinOrder {
    private List<String> tables = new ArrayList<>();
    private List<Expression> expressions;
    private List<Element> unionSet;
    private Map<String, Expression> tableMap = new HashMap<>();
    private List<Integer> tableIndex = new ArrayList<>();

    /**
     * This class is a helper class for dynamic programming, which stores the table list and expression list
     */
    private class Entry {
        private List<String> tableList = new ArrayList<>();
        private List<Expression> expressionList = new ArrayList<>();

        public Entry(List<String> tableList, List<Expression> expressionList) {
            this.tableList = tableList;
            this.expressionList = expressionList;
        }

        public List<String> getTableList() {
            return tableList;
        }

        public List<Expression> getExpressionList() {
            return expressionList;
        }

        public void add(String t, Expression e) {
            tableList.add(t);
            expressionList.add(e);
        }
    }

    public JoinOrder(LogicalUniqJoinOperator uniqJoinOperator, List<Element> unionSet) {
        List<LogicalOperator> operators = uniqJoinOperator.ChildrenOperators();
        this.unionSet = unionSet;
        for (LogicalOperator op : operators) {
            String t = "";
            Expression e = null;
            if (op instanceof LogicalScanOperator) {
                t = ((LogicalScanOperator) op).getTableName();
            } else if (op instanceof LogicalSelectionOperator) {
                t = ((LogicalScanOperator) ((LogicalSelectionOperator) op).getChildOperator()).getTableName();
                e = ((LogicalSelectionOperator) op).getSelectionCondition();
            }
            tables.add(t);
            expressions.add(e);
            tableMap.put(t, e);
        }
        dp();
    }

    /**
     * Dynamic programming for choosing join orders
     */
    private void dp() {
        int N = tables.size();
        double[][] cost = new double[N][N];
        Entry[][] tableToExpr = new Entry[N][N];

        for (int i = 0; i < N; i++) {
            List<String> tempList1 = new ArrayList<>();
            tempList1.add(tables.get(i));
            List<Expression> tempList2 = new ArrayList<>();
            tempList2.add(expressions.get(i));
            tableToExpr[0][i] = new Entry(tempList1, tempList2);
            cost[0][i] = 0;
        }

        for (int k = 1; k < N; k++) {
            for (int i = 0; i < N; i++) {
                double[] currCost = new double[N];
                for (int j = 0; j < N; j++) {
                    List<String> tableList = tableToExpr[k - 1][i].getTableList();
                    List<Expression> exprList = tableToExpr[k - 1][i].getExpressionList();
                    String t = tables.get(j);
                    Expression e = expressions.get(j);
                    if (!tableList.contains(t)) {
                        tableList.add(t);
                        exprList.add(e);
                        currCost[j] = getJoinSize(tableList);
                    }
                    else {
                        currCost[j] = 0;
                    }
                }
                int minIndex = argmin(currCost);
                tableToExpr[k][i].add(tables.get(minIndex), expressions.get(minIndex));
                cost[k][i] += currCost[minIndex];
            }
        }

        int minIndex = argmin(cost[N - 1]);
        for (String t : tableToExpr[N - 1][minIndex].getTableList()) {
            tableIndex.add(tables.indexOf(t));
        }
        expressions = tableToExpr[N - 1][minIndex].getExpressionList();
    }

    public List<Integer> getTableIndex() {
        return tableIndex;
    }

    /**
     * Getter method for list of tables
     * @return A list of table objects
     */
    public List<String> getTables() {
        return tables;
    }

    /**
     * Getter method for list of expressions
     * @return A list of expression objects
     */
    public List<Expression> getExpressions() {
        return expressions;
    }

    /**
     * Return the index of the min value in an array
     * @param a array
     * @return index
     */
    private int argmin(double[] a) {
        double min = Double.MAX_VALUE;
        int index = 0;
        for (int i = 0; i < a.length; i++) {
            if (min > a[i]) {
                min = a[i];
                index = i;
            }
        }
        return index;
    }

    /**
     * Compute intermediate relation sizes based on data statistics.
     * @param tableList A list of table objects
     * @return the join size
     */
    private double getJoinSize(List<String> tableList) {
        double result = 1.0;
        for (String t : tableList) {
            result *= DatabaseCatalog.getNumTuples(t);
        }
        for (Element e : unionSet) {
            Set<String> tableSet = new HashSet<>();
            for (Column c1 : e.getAttribute()) {
                for (Column c2 : e.getAttribute()) {
                    String t1 = c1.getTable().getName();
                    String t2 = c2.getTable().getName();
                    if (!tableSet.contains(t1 + t2) && !tableSet.contains(t2 + t1)) {
                        tableSet.add(t1 + t2);
                        tableSet.add(t2 + t1);
                        if (tableList.contains(t1) && tableList.contains(t2)) {
                            double v1 = getVValue(t1, c1);
                            double v2 = getVValue(t2, c2);
                            result /= Math.max(v1, v2);
                        }
                    }
                }
            }
        }
        if (result < 1) {
            result = 1.0;
        }
        return result;
    }

    /**
     * Compute and return V-value which is the number of distinct values that column c takes in table t
     * @param t Table t
     * @param c Column c
     * @return V-value
     */
    private double getVValue(String t, Column c) {
        double result = 1;
        int[] stats = DatabaseCatalog.getStats().get(c);
        Expression e = tableMap.get(t);

        if (e == null) {
            result = stats[1] - stats[0] + 1;
        } else {
            result = DatabaseCatalog.getReductionFactorClosed(t, c.getColumnName(), (long) stats[0], (long) stats[1]);
        }
        return result;
    }
}
