package edu.cornell.cs4321.Visitors;

import edu.cornell.cs4321.LogicalOperators.*;
import edu.cornell.cs4321.PhysicalOperators.*;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * A visitor class that recursively walks the logical plan and builds up a corresponding physical plan.
 * @author Jiangjie Man: jm2559
 */
public class PhysicalPlanBuilderVisitor {
    private List<Join> joinList;
    private Table fromTable;
    private List<SelectItem> projectionList;
    private Expression expr;
    private List<OrderByElement> orderByList;
    private Distinct d;
    private String alias;
    private Operator operator;
    private LogicalOperator logicalOperator;
    private String[] joinMethod;
    private String[] sortMethod;

    public PhysicalPlanBuilderVisitor(Statement statement, String[] joinMethod, String[] sortMethod) {
        // parse the statement
        Select select = (Select) statement;
        PlainSelect pSelect = (PlainSelect) select.getSelectBody();

        joinList = pSelect.getJoins();
        expr = pSelect.getWhere();
        projectionList = pSelect.getSelectItems();
        fromTable = (Table) pSelect.getFromItem();
        orderByList = pSelect.getOrderByElements();
        d = pSelect.getDistinct();
        alias = fromTable.getAlias();

        this.joinMethod = joinMethod;
        this.sortMethod = sortMethod;
        
        // build the logical query tree
        boolean useAlias = false;
        if (fromTable.getAlias() != null) {
            useAlias = true;
        }

        logicalOperator = new LogicalScanOperator(fromTable.getName(), alias);

        if (joinList == null) {
            // one scanner, one table, no need to extract different select
            // conditions
            logicalOperator = new LogicalSelectionOperator(logicalOperator, expr);
        } else {
            // deal with join relations
            List<Table> joinTables = new ArrayList<>();
            for (Join joinStatement : joinList) {
                joinTables.add((Table) joinStatement.getRightItem());
            }
            // Extract join expressions and group them by different tables.
            List<String> joinTableNames = new ArrayList<>();
            String fromName = useAlias ? fromTable.getAlias() : fromTable.getName();
            for (Table t : joinTables) {
                if (useAlias) {
                    joinTableNames.add(t.getAlias());
                } else {
                    joinTableNames.add(t.getName());
                }
            }

            JoinExpExtractVisitor visitor = new JoinExpExtractVisitor(fromName, joinTableNames);
            if(expr != null) {
                expr.accept(visitor); // now our visitor has grouped expressions
            }

            if (visitor.getSingleTableExpr(fromName) != null) {
                logicalOperator = new LogicalSelectionOperator(logicalOperator, visitor.getSingleTableExpr(fromName));
            }
            // Construct left-deep join operator tree.
            Iterator<Table> iterator = joinTables.iterator();
            while (iterator.hasNext()) {
                Table t = iterator.next();
                LogicalOperator joinOperand = new LogicalScanOperator(t.getName(), t.getAlias());
                String joinName = useAlias ? t.getAlias() : t.getName();
                if (visitor.getSingleTableExpr(joinName) != null) {
                    joinOperand = new LogicalSelectionOperator(joinOperand, visitor.getSingleTableExpr(joinName));
                }
                logicalOperator = new LogicalJoinOperator(logicalOperator, joinOperand, visitor.getJoinExpr(joinName));
            }
        }
        logicalOperator = new LogicalProjectionOperator(logicalOperator);
        logicalOperator = new LogicalSortOperator(logicalOperator, orderByList);
        logicalOperator = new LogicalDistinctOperator(logicalOperator);
    }

    /**
     * Getter method for the physical root operator.
     * @return physical root operator
     */
    public Operator getOperator() {
        return operator;
    }

    /**
     * Getter method for the logical root operator.
     * @return logical root operator
     */
    public LogicalOperator getLogicalOperator() {
        return logicalOperator;
    }

    /**
     * Visitor method for LogicalScanOperator.
     * @param scanOperator A LogicalScanOperator
     */
    public void visit(LogicalScanOperator scanOperator) {
        operator = new ScanOperator(scanOperator.getTableName(), scanOperator.getAlias());
    }

    /**
     * Visitor method for LogicalSelectionOperator.
     * @param selectionOperator A LogicalSelectionOperator
     */
    public void visit(LogicalSelectionOperator selectionOperator) {
        selectionOperator.getChildOperator().accept(this);
        operator = new SelectionOperator(operator, selectionOperator.getSelectionCondition());
    }

    /**
     * Visitor method for LogicalProjectionOperator.
     * @param projectionOperator A LogicalProjectionOperator
     */
    public void visit(LogicalProjectionOperator projectionOperator) {
        projectionOperator.getChildOperator().accept(this);
        operator = new ProjectionOperator(operator, projectionList);
    }

    /**
     * Visitor method for LogicalJoinOperator.
     * @param joinOperator A LogicalJoinOperator
     */
    public void visit(LogicalJoinOperator joinOperator) {
        joinOperator.getLeftChildOperator().accept(this);
        Operator leftOperator = operator;
        joinOperator.getRightChildOperator().accept(this);
        
        //use the join operator specified in the conf file
        if(joinMethod[0].equals("0")){
        	operator = new JoinOperator(leftOperator, operator, joinOperator.getJoinExpression());
        }else if(joinMethod[0].equals("1")){
        	int nPage = Integer.parseInt(joinMethod[1]);
        	operator = new BNLJOperator(leftOperator, operator, joinOperator.getJoinExpression(),nPage);
        }else{
        	int nPage = Integer.parseInt(joinMethod[1]);
        	//TODO: Deploy SMJ here
        }
        
    }
    /**
     * Visitor method for LogicalSortOperator.
     * @param sortOperator A LogicalSortOperator
     */
    public void visit(LogicalSortOperator sortOperator) {
        sortOperator.getChildOperator().accept(this);
        if (orderByList != null || (orderByList == null && d != null)) {
            operator = new SortOperator(operator, orderByList);
        }
    }

    /**
     * Visitor method for LogicalDistinctOperator.
     * @param distinctOperator A LogicalDistinctOperator
     */
    public void visit(LogicalDistinctOperator distinctOperator) {
        distinctOperator.getChildOperator().accept(this);
        if (d != null) {
            operator = new DuplicateEliminationOperator(operator);
        }
    }
}
