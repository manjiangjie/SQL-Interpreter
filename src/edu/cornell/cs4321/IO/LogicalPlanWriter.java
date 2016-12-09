package edu.cornell.cs4321.IO;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import edu.cornell.cs4321.LogicalOperators.*;
import edu.cornell.cs4321.UnionFind.Element;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.SelectItem;

public class LogicalPlanWriter {
	PrintWriter pw;
	
	public LogicalPlanWriter(String filename) throws FileNotFoundException {
		pw = new PrintWriter(new FileOutputStream(filename, false));
	}
	
	public void write(LogicalOperator operator) {
		writeLevel(operator, 0);
		pw.close();
	}
	
	public void writeLevel(LogicalOperator operator, int level) {
		if(operator instanceof LogicalDistinctOperator) {
			writeOperator((LogicalDistinctOperator)operator, level);
		}
		if(operator instanceof LogicalSortOperator) {
			writeOperator((LogicalSortOperator)operator, level);
		}
		if(operator instanceof LogicalProjectionOperator) {
			writeOperator((LogicalProjectionOperator)operator, level);
		}
		if(operator instanceof LogicalUniqJoinOperator) {
			writeOperator((LogicalUniqJoinOperator)operator, level);
		}
		if(operator instanceof LogicalSelectionOperator) {
			writeOperator((LogicalSelectionOperator)operator, level);
		}
		if(operator instanceof LogicalScanOperator) {
			writeOperator((LogicalScanOperator)operator, level);
		}
	}
	
	public void writeOperator(LogicalDistinctOperator operator, int level) {
		pw.write(getPreDashes(level));
		pw.write("DupElim"+"\n");
		writeLevel(operator.getChildOperator(), level+1);
	}
	
	public void writeOperator(LogicalSortOperator operator, int level) {
		pw.write(getPreDashes(level));
		pw.write("Sort[");
		String separator = "";
		for(Column c : operator.getSortByColumns()) {
			pw.write(separator + c.getWholeColumnName());
			if (separator.length() == 0) {
				separator = ", ";
			}
		}
		pw.write("]\n");
		writeLevel(operator.getChildOperator(), level+1);
	}
	
	public void writeOperator(LogicalProjectionOperator operator, int level) {
		// ignore "select *"
		for(SelectItem c : operator.getProjectList()) {
			if(c.toString().equals("*")) {
				writeLevel(operator.getChildOperator(), level);
				return;
			}
		}
		pw.write(getPreDashes(level));
		pw.write("Project[");
		String separator = "";
		for(SelectItem c : operator.getProjectList()) {
			pw.write(separator + c.toString());
			if (separator.length() == 0) {
				separator = ", ";
			}
		}
		pw.write("]\n");	
		writeLevel(operator.getChildOperator(), level+1);
	}
	
	public void writeOperator(LogicalUniqJoinOperator operator, int level) {
		pw.write(getPreDashes(level));
		pw.write("Join[");
		String separator = "";
		for(Expression e : operator.getResidualExpression()) {
			pw.write(separator + e.toString());
			if (separator.length() == 0) {
				separator = ", ";
			}
		}
		pw.write("]\n");	
		for(Element e : operator.getUnionFind().getUnionFind()) {
			pw.write("[");
			separator = "";
			pw.write("[");
			for(Column c : e.getAttribute()) {
				pw.write(separator + c.getWholeColumnName());
				if (separator.length() == 0) {
					separator = ", ";
				}
			}
			pw.write("]");
			pw.write(", equals " + String.valueOf(e.getEquality()));
			pw.write(", min " + String.valueOf(e.getLowerBound()));
			pw.write(", max " + String.valueOf(e.getUpperBound()));
			pw.write("]\n");
		}
		for(LogicalOperator op : operator.ChildrenOperators()) {
			writeLevel(op, level+1);
		}
	}
	
	public void writeOperator(LogicalSelectionOperator operator, int level) {
		pw.write(getPreDashes(level));
		pw.write("Select[" + operator.getSelectionCondition() + "]");
		pw.write("\n");
		writeLevel(operator.getChildOperator(), level+1);
	}
	
	public void writeOperator(LogicalScanOperator operator, int level) {
		pw.write(getPreDashes(level));
		pw.write("Leaf[" + operator.getTableName() + "]");
		pw.write("\n");	
	}
	
	public String getPreDashes(int level) {
		String result = "";
		for(int i=0;i<level;i++) {
			result += "-";
		}
		return result;
	}
}
