package edu.cornell.cs4321.Visitors;

import edu.cornell.cs4321.Database.DatabaseCatalog;
import edu.cornell.cs4321.Database.IndexInfo;
import edu.cornell.cs4321.JoinOrder;
import edu.cornell.cs4321.LogicalOperators.*;
import edu.cornell.cs4321.PhysicalOperators.*;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
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
    private int[] joinOrder;
    private int[] exprOrder;

    public PhysicalPlanBuilderVisitor(Statement statement, String tempDir) {
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

        joinMethod = new String[]{"1", "300"};
        sortMethod = new String[]{"1", "5"};
        this.tempDir = tempDir;
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
    public void visitLegacy(LogicalSelectionOperator selectionOperator) {
        selectionOperator.getChildOperator().accept(this);
        
        //TODO: whether or not use index, choose wisely here
        boolean useIndex = true;
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
     * Visitor method for LogicalSelectionOperator.
     * @param selectionOperator A LogicalSelectionOperator
     */
    public void visit(LogicalSelectionOperator selectionOperator) {
    	String tableName = ((LogicalScanOperator)selectionOperator.getChildOperator()).getTableName();
    	//handle alias
    	//tableName = "Reserves";
    	double costOfScan = DatabaseCatalog.getCostOfScan(tableName);
    	
    	double r = 1.0;
    	List<IndexInfo> idxInfos = DatabaseCatalog.getIndexInfoByTable(tableName);
    	int p = DatabaseCatalog.getNumPages(tableName);
    	int t = DatabaseCatalog.getNumTuples(tableName);    	
    	double minCostOfIndex = costOfScan;
    	IndexExpExtractVisitor visitorToUse = null;
    	IndexInfo indexInfo = null;
    	for(IndexInfo idxInfo : idxInfos) {
    		String columnName = idxInfo.getColumn().getColumnName();
    		IndexExpExtractVisitor visitor = new IndexExpExtractVisitor(columnName);    	
    		Expression selectionCondition = selectionOperator.getSelectionCondition();
    		selectionCondition.accept(visitor);
    		
    		int l = DatabaseCatalog.getNumLeaves(tableName, columnName);
        	r = DatabaseCatalog.getReductionFactor(tableName, columnName, visitor.getLowkey(), visitor.getHighkey(), visitor.isLowOpen(), visitor.isHighOpen());
        	double costOfIndex;
        	if(idxInfo.isClustered()) {
        		costOfIndex = 3 + p*r;
        	} else {
        		costOfIndex = 3 + l*r + t*r;
        	}
        	
        	if(costOfIndex < minCostOfIndex) {
        		minCostOfIndex = costOfIndex;
        		visitorToUse = visitor;
        		indexInfo = idxInfo;
        	}
        	
    	}
    	if(null != visitorToUse) { //Use index
    		LogicalScanOperator scanOperator = (LogicalScanOperator) selectionOperator.getChildOperator();
    		operator = new IndexScanOperator(scanOperator.getTableName(), scanOperator.getAlias(),visitorToUse.getLowkey(),visitorToUse.getHighkey(),visitorToUse.isLowOpen(),visitorToUse.isHighOpen(),indexInfo);
			if(visitorToUse.getExprWithoutIndex() != null){
				operator = new SelectionOperator(operator, visitorToUse.getExprWithoutIndex());
			}
    	} else { // Use Plain Scan
    		selectionOperator.getChildOperator().accept(this);
    		operator = new SelectionOperator(operator, selectionOperator.getSelectionCondition());
    	}
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
    
    /**
     * Visitor method for LogicalUniqJoinOperator.
     * @param logicalUniqJoinOperator A LogicalDistinctOperator
     */
    public void visit(LogicalUniqJoinOperator logicalUniqJoinOperator) {
        JoinOrder joinOrder = new JoinOrder(logicalUniqJoinOperator, logicalUniqJoinOperator.getUnionFind().getUnionFind());
        List<Integer> tableIndex = joinOrder.getTableIndex();
        List<Expression> expressions = joinOrder.getExpressions();
    	List<LogicalOperator> children = logicalUniqJoinOperator.ChildrenOperators();
    	LogicalOperator child = children.get(tableIndex.get(0));
    	child.accept(this);
    	for(int i = 1; i < tableIndex.size(); i++){
    		Operator leftOperator = operator;
    		children.get(tableIndex.get(i)).accept(this);
    		Expression exp = exprs.get(exprOrder[i-1]);
    		
    		//check what join method would be apply below
    		
    		if(!(exp instanceof EqualsTo)){//TODO: Select join method
            	int nPage = Integer.valueOf(joinMethod[1]);//set block size for BNLJ
            	operator = new BNLJOperator(leftOperator, operator, exp, nPage);
            }else{
            	// SMJ
            	JoinAttrExtractVisitor visitor = new JoinAttrExtractVisitor();
            	exp.accept(visitor);
            	//insert sort operator to leftChild
            	//external sort
            	int nPage = Integer.valueOf(sortMethod[1]);//set number of pages for ES
            	leftOperator = new ExternalSortOperator(leftOperator, visitor.getLeftAttrList(), nPage, tempDir);
            	
            	//insert sort operator to rightChild
            	Operator rightOperator = null;
            	rightOperator = new ExternalSortOperator(operator, visitor.getRightAttrList(), nPage, tempDir);
            	operator = new SMJOperator(leftOperator, rightOperator, exp);
            }
    	}
    }
}
