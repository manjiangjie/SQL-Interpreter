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
import edu.cornell.cs4321.LogicalOperators.LogicalUniqJoinOperator;
import edu.cornell.cs4321.PhysicalOperators.Operator;
import edu.cornell.cs4321.UnionFind.Element;
import edu.cornell.cs4321.Visitors.JoinExpExtractVisitor;
import edu.cornell.cs4321.Visitors.UnionFindVisitor;
import net.sf.jsqlparser.expression.BinaryExpression;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.GreaterThanEquals;
import net.sf.jsqlparser.expression.operators.relational.MinorThanEquals;
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
        
        boolean isUniqJoin = true;

        if (joinList == null) {
            // one scanner, one table, no need to extract different select
            // conditions
        	if (expr != null) {
        		logicalOperator = new LogicalSelectionOperator(logicalOperator, expr);
        	}            
        } else {
        	// Retrieve all tables
            List<Table> allTables = new ArrayList<>();
            List<LogicalScanOperator> logicalScanOperators = new ArrayList<>();
            allTables.add(fromTable);
            logicalScanOperators.add((LogicalScanOperator) logicalOperator);
            
            for (Join joinStatement : joinList) {
            	Table t = (Table) joinStatement.getRightItem();
            	allTables.add(t);
            	logicalScanOperators.add(new LogicalScanOperator(t.getName(),t.getAlias()));
            }
            // Generate union-find elements            
            UnionFindVisitor visitor = new UnionFindVisitor();
            if(expr != null) {
            	expr.accept(visitor);
            }
            List<Element> ufElements = visitor.getUnionFind().getUnionFind();
            LogicalUniqJoinOperator logicalOperator = new LogicalUniqJoinOperator(visitor.getResidual());
            for(int i = 0; i < allTables.size(); i++) {
            	Table t = allTables.get(i);
            	LogicalOperator leafScanOperator = logicalScanOperators.get(i);
            	String tableRef = useAlias ? t.getAlias() : t.getName();
            	Expression singleTableExpr = null;
            	for(Element e : ufElements) {
            		for(Column c : e.getAttribute()) {
            			if(c.getTable().getName().equals(tableRef)) {
            				if(e.getEquality() != null) {
            					EqualsTo eq = new EqualsTo(c,new LongValue(e.getEquality()));
            					singleTableExpr = this.addExpression(singleTableExpr, eq);
            				} else {
            					if(e.getLowerBound() != null) {
            						GreaterThanEquals greatEq = new GreaterThanEquals(c,new LongValue(e.getLowerBound()));
            						singleTableExpr = this.addExpression(singleTableExpr, greatEq);
            					}
            					if(e.getUpperBound() != null) {
            						MinorThanEquals minorEq = new MinorThanEquals(c,new LongValue(e.getLowerBound()));
            						singleTableExpr = this.addExpression(singleTableExpr, minorEq);
            					}
            				}
            			}
            		}
            	}
            	for(Expression expr : visitor.getResidual()) {
            		BinaryExpression e = (BinaryExpression)expr;
            		if( (e.getLeftExpression() instanceof Column) && (e.getRightExpression() instanceof LongValue)) {
            			Column c = (Column)(e.getLeftExpression());
            			if(c.getTable().getName().equals(tableRef)) {
            				singleTableExpr = this.addExpression(singleTableExpr, e);
            			}
            		}
            		if( (e.getRightExpression() instanceof Column) && (e.getLeftExpression() instanceof LongValue)) {
            			Column c = (Column)(e.getRightExpression());
            			if(c.getTable().getName().equals(tableRef)) {
            				singleTableExpr = this.addExpression(singleTableExpr, e);
            			}
            		}
            	}
            	if(singleTableExpr != null) {
            		LogicalSelectionOperator logicalSelectionOperator = new LogicalSelectionOperator(leafScanOperator, singleTableExpr);
            		logicalOperator.addOperator(logicalSelectionOperator);
            	} else {
            		logicalOperator.addOperator(leafScanOperator);
            	}
            }

        }
        // Fixed Hierarchy
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
	private Expression addExpression(Expression originalExpression, Expression newExpression) {
		if(originalExpression == null) return newExpression;
		return new AndExpression(originalExpression, newExpression);
	}
}
