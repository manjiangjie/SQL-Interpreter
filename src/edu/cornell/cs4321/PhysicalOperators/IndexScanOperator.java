package edu.cornell.cs4321.PhysicalOperators;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import edu.cornell.cs4321.BPlusTree.DataEntry;
import edu.cornell.cs4321.Database.DatabaseCatalog;
import edu.cornell.cs4321.Database.IndexInfo;
import edu.cornell.cs4321.Database.Tuple;
import edu.cornell.cs4321.IO.BPlusTreeDeserializer;
import edu.cornell.cs4321.IO.BinaryTupleReader;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;

/**
 * @author Hao Qian hq43
 * 
 * IndexScanOperator retrieves matched data entries(leaf nodes in BPlusTree).
 */
public class IndexScanOperator extends Operator {
	
	private String tableName;
	private String alias;
	private Long lowkey;
	private Long highkey;
	private Boolean lowOpen;
	private Boolean highOpen;
	private IndexInfo indexInfo;
	private List<DataEntry> dataEntries;
	private BPlusTreeDeserializer deserializer;
	private ListIterator<DataEntry> iterator;
	private BinaryTupleReader tr;
	private boolean entryFound; // for clustered index
	
	public IndexScanOperator(String tableName, String alias, Long lowkey, Long highkey, Boolean lowOpen, Boolean highOpen, IndexInfo indexInfo) {
		this.tableName = tableName;
		this.alias = alias;
		this.lowkey = lowkey;
		this.highkey = highkey;
		this.lowOpen = lowOpen;
		this.highOpen = highOpen;
		this.indexInfo = indexInfo;
		this.deserializer = new BPlusTreeDeserializer(indexInfo);
		this.entryFound = false;
		if(!indexInfo.isClustered()) {
			this.dataEntries = deserializer.getDataEntriesByIndex(lowkey, highkey, lowOpen, highOpen);
			this.iterator = this.dataEntries.listIterator();
		}
		
		if (alias != null) {
			List<Column> newSchemaList = new ArrayList<Column>();
			List<Column> schemaList = DatabaseCatalog.getSchemaByTable(tableName);
			for (Column c : schemaList) {
				Table t = new Table();
				t.setName(alias);
				Column newColumn = new Column();
				newColumn.setTable(t);
				newColumn.setColumnName(c.getColumnName());
				newSchemaList.add(newColumn);
			}
			DatabaseCatalog.setSchemaByTable(tableName, newSchemaList);
		}
		this.tr = new BinaryTupleReader(tableName);
	}

	@Override
	public Tuple getNextTuple() {
		if(this.indexInfo.isClustered()){
			if(!entryFound){
				DataEntry entry = deserializer.getLeftMostEntry(lowkey, lowOpen, highkey, highOpen);
				if(entry!=null){
					entryFound = true;
					tr.reset(entry.getPageId(), entry.getTupleId());
					return tr.readNextTuple();
				}
				return null;
			} else { //tuple t, check if in the range
				Tuple t = tr.readNextTuple();
				if(t!=null){
					int val = t.getValueByCol(indexInfo.getColumn());
					if( highOpen.booleanValue() && val < highkey ||
						!highOpen.booleanValue() && val <= highkey ) {
						return t;
					}
				}				
				return null;
			}
		} else {
			if(this.iterator.hasNext()){
				DataEntry entry = this.iterator.next();
				int pageId = entry.getPageId();
				int tupleId = entry.getTupleId();			
				tr.reset(pageId, tupleId);
				Tuple t = tr.readNextTuple();
				return t;
			} else {
				return null;
			}
		}		
	}

	@Override
	public void reset() {
		tr.reset();
		if(!indexInfo.isClustered()) {
			this.iterator = this.dataEntries.listIterator();
		}
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
