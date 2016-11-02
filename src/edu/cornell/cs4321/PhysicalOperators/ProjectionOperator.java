package edu.cornell.cs4321.PhysicalOperators;

import java.util.List;

import edu.cornell.cs4321.Database.Tuple;
import net.sf.jsqlparser.statement.select.SelectItem;

/**
 * Projection operator to deal with SELECT clause. 
 * @author Chenxi Su cs2238, Hao Qian hq43, Jiangjie Man jm2559
 *
 */

public class ProjectionOperator extends Operator{
	
	private Operator childOperator;
	private List<SelectItem> projectList;
	
	/**
	 * Construct ProjectionOperator with child operator and a list attributes to project.
	 */
	public ProjectionOperator(Operator op, List<SelectItem> projectList){
		childOperator = op;
		this.projectList = projectList;
	}

	/**
	 * Generate a subset tuple of child operator's tuple retrieved.
	 * @return projected tuple
	 */
	@Override
	public Tuple getNextTuple() {
		Tuple temp, result = null;
		while( (temp = childOperator.getNextTuple()) != null){
			//Empty projection: When select *
			if(projectList.size() == 1 && projectList.get(0).toString().equals("*")){
				result = temp;
				break;
			}
			// Solid projection
			//System.out.println("Before Projection: "+ temp);
			result = new Tuple(temp, projectList);
			break;
		}
		return result;
	}

	/**
	 * Reset child operator's class.
	 */
	@Override
	public void reset() {
		childOperator.reset();		
	}

	@Override
	public void reset(int index) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getIndex() {
		// TODO Auto-generated method stub
		return 0;
	}

}
