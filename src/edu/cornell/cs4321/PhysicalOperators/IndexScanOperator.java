package edu.cornell.cs4321.PhysicalOperators;

import edu.cornell.cs4321.Database.IndexInfo;
import edu.cornell.cs4321.Database.Tuple;

public class IndexScanOperator extends Operator {
	
	private String tableName;
	private String alias;
	private Long lowkey;
	private Long highkey;
	private Boolean lowOpen;
	private Boolean highOpen;
	private IndexInfo indexInfo;
	
	public IndexScanOperator(String tableName, String alias, Long lowkey, Long highkey, Boolean lowOpen, Boolean highOpen, IndexInfo indexInfo) {
		this.tableName = tableName;
		this.alias = alias;
		this.lowkey = lowkey;
		this.highkey = highkey;
		this.lowOpen = lowOpen;
		this.highOpen = highOpen;
		this.indexInfo = indexInfo;
	}

	@Override
	public Tuple getNextTuple() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void reset() {
		// TODO Auto-generated method stub

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
