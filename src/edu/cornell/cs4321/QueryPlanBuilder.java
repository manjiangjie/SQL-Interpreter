package edu.cornell.cs4321;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import edu.cornell.cs4321.Operators.DuplicateEliminationOperator;
import edu.cornell.cs4321.Operators.JoinOperator;
import edu.cornell.cs4321.Operators.Operator;
import edu.cornell.cs4321.Operators.ProjectionOperator;
import edu.cornell.cs4321.Operators.ScanOperator;
import edu.cornell.cs4321.Operators.SelectionOperator;
import edu.cornell.cs4321.Operators.SortOperator;
import edu.cornell.cs4321.Visitors.JoinExpExtractVisitor;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.Distinct;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.OrderByElement;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.SelectItem;

/**
 * This class is used to construct a query plan tree which consists of operator
 * nodes. Call getNextTuple() on the root operator repeatedly will output all
 * tuples you need.
 * 
 * @author Chenxi Su cs2238, Hao Qian hq43, Jiangjie Man jm2559
 *
 */
public class QueryPlanBuilder {

	private Operator rootOperator;

	public Operator getRootOperator() {
		return this.rootOperator;
	}

	/**
	 * Constructor: Based on the PlainSelect which represents the Query,
	 * generate the tree structured query plan
	 * 
	 * @param PlainSelect
	 */
	public QueryPlanBuilder(PlainSelect pSelect) {
		List<Join> joinList = pSelect.getJoins();
		Expression expr = pSelect.getWhere();
		List<SelectItem> projectionList = pSelect.getSelectItems();
		boolean useAlias = false;
		Table fromTable = (Table) pSelect.getFromItem();
		List<OrderByElement> orderByList = pSelect.getOrderByElements();
		List<Column> orderByCols = new LinkedList<Column>();
		if (orderByList != null) {
			for (OrderByElement e : orderByList) {
				Column c = (Column) e.getExpression();
				orderByCols.add(c);
			}
		}

		Distinct d = pSelect.getDistinct();
		if (fromTable.getAlias() != null)
			useAlias = true;
		Operator operator;
		if (joinList == null) {
			// one scanner, one table, no need to extract different select
			// conditions
			ScanOperator scanner = new ScanOperator(fromTable.getName());
			if (useAlias) {
				scanner = new ScanOperator(fromTable.getName(), fromTable.getAlias());
			}
			operator = new SelectionOperator(scanner, expr);
			operator = new ProjectionOperator(operator, projectionList);
		} else {
			// deal with join relations
			List<Table> joinTables = new ArrayList<Table>();
			for (Join joinStatement : joinList) {
				joinTables.add((Table) joinStatement.getRightItem());
			}
			// Extract join expressions and group them by different tables.
			JoinExpExtractVisitor visitor;
			if (useAlias) {
				List<String> joinTableNames = new ArrayList<String>();
				for (Table t : joinTables) {
					joinTableNames.add(t.getAlias());
				}
				visitor = new JoinExpExtractVisitor(fromTable.getAlias(), joinTableNames);
			} else {
				List<String> joinTableNames = new ArrayList<String>();
				for (Table t : joinTables) {
					joinTableNames.add(t.getName());
				}
				visitor = new JoinExpExtractVisitor(fromTable.getName(), joinTableNames);
			}
			if(expr != null)
				expr.accept(visitor); // now our visitor has grouped expressions
			
			String fromName = useAlias ? fromTable.getAlias() : fromTable.getName();
			operator = useAlias ? new ScanOperator(fromTable.getName(), fromTable.getAlias())
					: new ScanOperator(fromTable.getName());
			if (visitor.getSingleTableExpr(fromName) != null) {
				operator = new SelectionOperator(operator, visitor.getSingleTableExpr(fromName));
			}
			// Construct left-deep join operator tree.
			Iterator<Table> iterator = joinTables.iterator();
			while (iterator.hasNext()) {
				Table t = iterator.next();
				Operator joinOperand = useAlias ? new ScanOperator(t.getName(), t.getAlias())
						: new ScanOperator(t.getName());
				String joinName = useAlias ? t.getAlias() : t.getName();
				if (visitor.getSingleTableExpr(joinName) != null) {
					joinOperand = new SelectionOperator(joinOperand, visitor.getSingleTableExpr(joinName));
				}
				operator = new JoinOperator(operator, joinOperand, visitor.getJoinExpr(joinName));
			}
			operator = new ProjectionOperator(operator, projectionList);

		}

		if (orderByList != null) {
			operator = new SortOperator(operator, orderByCols);
		}
		// If there's a DISTINCT but no ORDER BY statement, then add one sort
		// operator.
		if (d != null) {
			if (orderByList == null) {
				operator = new SortOperator(operator);
			}
			operator = new DuplicateEliminationOperator(operator);
		}

		this.rootOperator = operator;
	}
}
