package edu.cornell.cs4321.Database;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectItem;

/**
 * Core data structure, deal with operations on tuples
 * @author Chenxi Su cs2238, Hao Qian hq43, Jiangjie Man jm2559
 */
public class Tuple {
	
	private HashMap<Column, Integer> tupleMap = new HashMap<>();
	private List<Column> schemaList;
	private String record;
	
	/**
	 * Constructor 1: Construct a tuple given attributes and a record.
	 * @param schemaList schema of the table
	 * @param record a line in a table, columns separated by comma
	 */
	public Tuple(List<Column> schemaList, String record) {
		this.schemaList = schemaList;
		String[] records = record.split(",");
		int n = records.length;
		for(int i = 0; i<n; i++){
			tupleMap.put(schemaList.get(i), Integer.parseInt(records[i]));
		}
		this.record = record;
	}	
	
	/**
	 * Constructor 2: for joinOperator to generate new tuple
	 * @param t1 tuple from left(outer) table
	 * @param t2 tuple from right(inner) table
	 * */
	public Tuple(Tuple t1, Tuple t2){
		List<Column> schema1 = t1.getSchema();
		List<Column> schema2 = t2.getSchema();
		List<Column> newSchema = new ArrayList<Column>(schema1);
		newSchema.addAll(schema2);
		this.schemaList = newSchema;
		HashMap<Column, Integer> map1 = t1.getTupleMap();
		HashMap<Column, Integer> map2 = t2.getTupleMap();
		for(int i = 0; i<schema1.size(); i++){
			tupleMap.put(schema1.get(i), map1.get(schema1.get(i)) );
		}
		for(int i = 0; i<schema2.size(); i++){
			tupleMap.put(schema2.get(i), map2.get(schema2.get(i)));
		}
		StringBuilder sb = new StringBuilder();
		sb.append(t1.getRecord());
		sb.append(",");
		sb.append(t2.getRecord());
		record = sb.toString();
	}

	/**
	 * Constructor 3: for projectionOperator to generate new subset tuple
	 * @param t Original tuple
	 * @param projectionList attribute list to project
	 * */
	public Tuple(Tuple t, List<SelectItem> projectionList){
		//initialize schemaList from projectionList
		this.schemaList = new LinkedList<>();
		
		for(SelectItem si : projectionList){
			String[] itemArr= si.toString().split("\\.");
			Column c = new Column();
			Table table = new Table();
			table.setName(itemArr[0]);
			c.setTable(table);
			c.setColumnName(itemArr[1]);
			this.schemaList.add(c);
			
		}
		StringBuilder sb = new StringBuilder();
		int n = schemaList.size();
		for(int i = 0; i<n; i++){
			tupleMap.put(schemaList.get(i), t.getValueByCol(schemaList.get(i)));
			sb.append(t.getValueByCol(schemaList.get(i)));
			if(i!=n-1){
				sb.append(",");
			}
		}
		record = sb.toString();
	}
	
	/**
	 * Constructor 4: generate a new Tuple based on new order of tables.
	 * @param t: original tuple
	 * @param tables: table references in new order.
	 */
	public Tuple(Tuple t, Statement statement) {
		Select select = (Select) statement;
        PlainSelect pSelect = (PlainSelect) select.getSelectBody();

        Table fromTable = (Table) pSelect.getFromItem();
        List<Join> joinList = pSelect.getJoins();
        List<String> tableRefs = new LinkedList<String>();
        boolean hasAlias = (fromTable.getAlias() != null);
        
        tableRefs.add(hasAlias? fromTable.getAlias() : fromTable.getName());
        for(Join join : joinList) {
        	Table joinTable = (Table) join.getRightItem();
        	tableRefs.add(hasAlias? joinTable.getAlias() : joinTable.getName());
        }
        
        this.schemaList = new LinkedList<>();
        for(String tr : tableRefs) {
        	for(Column c : t.getSchema()) {
        		if(c.getTable().getName().equals(tr)) {
        			this.schemaList.add(c);
        		}
        	}
        }

		StringBuilder sb = new StringBuilder();
		int n = schemaList.size();
		for(int i = 0; i<n; i++){
			tupleMap.put(schemaList.get(i), t.getValueByCol(schemaList.get(i)));
			sb.append(t.getValueByCol(schemaList.get(i)));
			if(i!=n-1){
				sb.append(",");
			}
		}
		record = sb.toString();
	}
	
	/**
	 * get the schema list
	 * @return the schemaList of this tuple
	 * */
	public List<Column> getSchema(){
		return this.schemaList;
	}
	
	/**
	 * get (Attribute,Value) pairs
	 * @return the HashMap representing attribute-value pairs
	 */
	public HashMap<Column, Integer> getTupleMap(){
		return tupleMap;
	}
	
	/**
	 * get value by column
	 * @param column the attribute to query
	 * @return the value of a specific column, return null if column doesn't exists
	 */
	public Integer getValueByCol(Column column){
		for(Entry<Column, Integer> e : tupleMap.entrySet()){
			String col = e.getKey().getColumnName();
			Integer val = e.getValue();
			if(col.equals(column.getColumnName()) && e.getKey().getTable().getName().equals(column.getTable().getName())){
				return val;
			}
		}
		return null;
	}

	/**
	 * A method for getting all the values in a Tuple.
	 * @return A list of integers which contains the values in the Tuple.
     */
	public List<Integer> getValues() {
		List<Integer> values = new ArrayList<>();
		for (Column c: schemaList) {
			values.add(getValueByCol(c));
		}
		return values;
	}
	
	/***
	 * get record
	 * @return the record of this tuple, columns separated by commas.
	 */
	public String getRecord(){
		return record;
	}

	/**
	 * get record
	 * @return the record of this tuple, columns separated by commas.
	 * */
	public String toString(){
		return record;
	}
	
}
