package edu.cornell.cs4321.IO;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import edu.cornell.cs4321.LogicalOperators.*;
import edu.cornell.cs4321.PhysicalOperators.*;
import edu.cornell.cs4321.UnionFind.Element;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.SelectItem;

public class PhysicalPlanWriter {
	PrintWriter pw;
	
	public PhysicalPlanWriter(String filename) throws FileNotFoundException {
		pw = new PrintWriter(new FileOutputStream(filename, false));
	}
	
	public void write(Operator operator) {
		writeLevel(operator, 0);
		pw.close();
	}
	
	public void writeLevel(Operator operator, int level) {
		if(operator instanceof DuplicateEliminationOperator) {
			writeOperator((DuplicateEliminationOperator)operator, level);
		}
		if(operator instanceof ExternalSortOperator) {
			writeOperator((ExternalSortOperator)operator, level);
		}
		if(operator instanceof ProjectionOperator) {
			writeOperator((ProjectionOperator)operator, level);
		}
		if(operator instanceof BNLJOperator) {
			writeOperator((BNLJOperator)operator, level);
		}
		if(operator instanceof SMJOperator) {
			writeOperator((SMJOperator)operator, level);
		}
		if(operator instanceof SelectionOperator) {
			writeOperator((SelectionOperator)operator, level);
		}
		if(operator instanceof ScanOperator) {
			writeOperator((ScanOperator)operator, level);
		}
		if(operator instanceof IndexScanOperator) {
			writeOperator((IndexScanOperator)operator, level);
		}
	}
	
	public void writeOperator(DuplicateEliminationOperator operator, int level) {
		pw.write(getPreDashes(level));
		pw.write("DupElim"+"\n");
		writeLevel(operator.getChildOperator(), level+1);
	}
	
	public void writeOperator(ExternalSortOperator operator, int level) {
		pw.write(getPreDashes(level));
		pw.write("ExternalSort[");
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
	
	public void writeOperator(ProjectionOperator operator, int level) {
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
	
	public void writeOperator(BNLJOperator operator, int level) {
		pw.write(getPreDashes(level));
		pw.write("BNLJ[");
		Expression catExpr = operator.getJoinExpression();
		pw.write(String.valueOf(catExpr));
		pw.write("]\n");
		writeLevel(operator.getLeftChild(), level+1);
		writeLevel(operator.getRightChild(), level+1);
	}
	
	public void writeOperator(SMJOperator operator, int level) {
		pw.write(getPreDashes(level));
		pw.write("SMJ[");
		Expression catExpr = operator.getJoinExpression();
		pw.write(String.valueOf(catExpr));
		pw.write("]\n");
		writeLevel(operator.getLeftChild(), level+1);
		writeLevel(operator.getRightChild(), level+1);
	}
	
	public void writeOperator(SelectionOperator operator, int level) {
		pw.write(getPreDashes(level));
		pw.write("Select[" + operator.getSelectionCondition() + "]");
		pw.write("\n");
		writeLevel(operator.getChildOperator(), level+1);
	}
	
	public void writeOperator(ScanOperator operator, int level) {
		pw.write(getPreDashes(level));
		pw.write("TableScan[" + operator.getTableName() + "]");
		pw.write("\n");	
	}
	
	public void writeOperator(IndexScanOperator operator, int level) {
		pw.write(getPreDashes(level));
		String s = operator.getTableName();
		s += ", " + operator.getColumnName();
		s += ", " + String.valueOf(operator.getLowKey());
		s += ", " + String.valueOf(operator.getHighKey());
		pw.write("IndexScan[" + s + "]");
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
