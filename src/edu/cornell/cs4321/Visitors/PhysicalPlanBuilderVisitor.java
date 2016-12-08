package edu.cornell.cs4321.Visitors;

import edu.cornell.cs4321.Database.DatabaseCatalog;
import edu.cornell.cs4321.Database.IndexInfo;
import edu.cornell.cs4321.LogicalOperators.*;
import edu.cornell.cs4321.PhysicalOperators.*;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
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
    private List<Column> orderByList;
    private Distinct d;
    private String alias;
    private Operator operator;
    private LogicalOperator logicalOperator;
    private String[] joinMethod;
    private String[] sortMethod;
    private String tempDir;
    private boolean useIndex;

    public PhysicalPlanBuilderVisitor(Statement statement, String[] joinMethod, String[] sortMethod, String tempDir, boolean useIndex) {
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

        this.joinMethod = joinMethod;
        this.sortMethod = sortMethod;
        this.tempDir = tempDir;
        this.useIndex = useIndex;
        
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
        if(useIndex){
        	LogicalScanOperator scanOperator = (LogicalScanOperator) selectionOperator.getChildOperator();
        	IndexInfo indexInfo = DatabaseCatalog.getIndexInfoByTable(scanOperator.getTableName()).get(0);
        	if(indexInfo != null) {
        		IndexExpExtractVisitor visitor = new IndexExpExtractVisitor(indexInfo.getColumn().getColumnName());
        		Expression selectionCondition = selectionOperator.getSelectionCondition();
        		if(selectionCondition != null)
        			selectionCondition.accept(visitor);
        		// Now selection conditions are divided into two groups in visitor.
        		if( (visitor.getHighkey() != null) || (visitor.getLowkey() != null) ){
        			operator = new IndexScanOperator(scanOperator.getTableName(), scanOperator.getAlias(),visitor.getLowkey(),visitor.getHighkey(),visitor.isLowOpen(),visitor.isHighOpen(),indexInfo);
        			if(visitor.getExprWithoutIndex() != null){
        				operator = new SelectionOperator(operator, visitor.getExprWithoutIndex());
        			}
        			return;
        		}
        	}
        }
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
        	// SMJ
        	Expression joinExpr = joinOperator.getJoinExpression();
        	JoinAttrExtractVisitor visitor = new JoinAttrExtractVisitor();
        	joinExpr.accept(visitor);
        	//insert sort operator to leftChild
        	if(sortMethod[0].equals("0")){
        		leftOperator = new SortOperator(leftOperator, visitor.getLeftAttrList());
        	}else if(sortMethod[0].equals("1")){//external sort
        		int nPage = Integer.parseInt(sortMethod[1]);
        		leftOperator = new ExternalSortOperator(leftOperator, visitor.getLeftAttrList(), nPage, tempDir);
        	}
        	//insert sort operator to rightChild
        	Operator rightOperator = null;
        	if(sortMethod[0].equals("0")){
        		rightOperator = new SortOperator(operator, visitor.getRightAttrList());
        	}else if(sortMethod[0].equals("1")){//external sort
        		int nPage = Integer.parseInt(sortMethod[1]);
        		rightOperator = new ExternalSortOperator(operator, visitor.getRightAttrList(), nPage, tempDir);
        	}
        	
        	operator = new SMJOperator(leftOperator, rightOperator, joinExpr);
        }
        
    }
    /**
     * Visitor method for LogicalSortOperator.
     * @param sortOperator A LogicalSortOperator
     */
    public void visit(LogicalSortOperator sortOperator) {
        sortOperator.getChildOperator().accept(this);
        if (orderByList != null || (orderByList == null && d != null)) {
        	if(sortMethod[0].equals("0")){
        		operator = new SortOperator(operator, orderByList);
        	}else if(sortMethod[0].equals("1")){//external sort
        		int nPage = Integer.parseInt(sortMethod[1]);
        		operator = new ExternalSortOperator(operator, orderByList, nPage, tempDir);
        	}
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
