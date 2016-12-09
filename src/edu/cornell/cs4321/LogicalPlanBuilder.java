package edu.cornell.cs4321;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import edu.cornell.cs4321.LogicalOperators.LogicalDistinctOperator;
import edu.cornell.cs4321.LogicalOperators.LogicalJoinOperator;
import edu.cornell.cs4321.LogicalOperators.LogicalOperator;
import edu.cornell.cs4321.LogicalOperators.LogicalProjectionOperator;
import edu.cornell.cs4321.LogicalOperators.LogicalScanOperator;
import edu.cornell.cs4321.LogicalOperators.LogicalSelectionOperator;
import edu.cornell.cs4321.LogicalOperators.LogicalSortOperator;
import edu.cornell.cs4321.PhysicalOperators.Operator;
import edu.cornell.cs4321.Visitors.JoinExpExtractVisitor;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.Distinct;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.OrderByElement;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectItem;

public class LogicalPlanBuilder {
    private List<Join> joinList;
    private Table fromTable;
    private List<SelectItem> projectionList;
    private Expression expr;
    private List<Column> orderByList;
    private Distinct d;
    private String alias;
    private LogicalOperator logicalOperator;
    
	public LogicalPlanBuilder(Statement statement) {
		// parse the statement
        Select select = (Select) statement;
        PlainSelect pSelect = (PlainSelect) select.getSelectBody();

        joinList = pSelect.getJoins();
        expr = pSelect.getWhere();
        projectionList = pSelect.getSelectItems();
        fromTable = (Table) pSelect.getFromItem();
        orderByList = new LinkedList<Column>();
        List<OrderByElement> orderByColumns = pSelect.getOrderByElements();
        if(orderByColumns != null) {
            for(OrderByElement e : orderByColumns) {
            	orderByList.add((Column)e.getExpression());
            }        	
        }
        d = pSelect.getDistinct();
        alias = fromTable.getAlias();
        
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
        if(orderByList!=null && !orderByList.isEmpty()){
            logicalOperator = new LogicalSortOperator(logicalOperator, orderByList);
        }
        if(d!=null){
        	if(orderByList.isEmpty()){
        		logicalOperator = new LogicalSortOperator(logicalOperator, orderByList);
        	}
            logicalOperator = new LogicalDistinctOperator(logicalOperator);
        }
	}
	public LogicalOperator getRootLogicalOperator() {
		return this.logicalOperator;
	}
}
