package edu.cornell.cs4321;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;

import org.junit.Test;

import edu.cornell.cs4321.BPlusTree.*;
import edu.cornell.cs4321.Database.DatabaseCatalog;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;


public class tests {

	@Test
	public void test() {
		Column c =  new Column();
		Table t = new Table();
		t.setName("Sailors");
		c.setTable(t);
		c.setColumnName("B");
		DatabaseCatalog.getInstance("samples/input");
		BPlusTree b = new BPlusTree(false, "Sailors", c, 2);
		indexNode root = b.getRoot();
		System.out.println(Arrays.toString(root.getKeys().toArray()));
		if(root.isUpperLayer())
			bfs(root.getIndexChildren());
	}
	
	public void bfs(ArrayList<indexNode> input){
		ArrayList<indexNode> next = new ArrayList<indexNode>();
		ArrayList<leafNode> leaves = new ArrayList<leafNode>();
		for(indexNode eachNode: input){
			if(eachNode == null)
				System.out.print(null + " ");
			else{
				System.out.print(Arrays.toString(eachNode.getKeys().toArray())+" ");
				if(eachNode.isUpperLayer())
					next.addAll(eachNode.getIndexChildren());
				else
					leaves.addAll(eachNode.getChildren());
			}
		}
		System.out.println();
		if(!leaves.isEmpty()){
			for(leafNode ln : leaves){
				System.out.print(Arrays.toString(ln.getMap().keySet().toArray())+" ");
			}
		}
		
		
		if(!next.isEmpty())
			bfs(next);
		else
			return;
	}

}
