package edu.cornell.cs4321;

import java.util.ArrayList;
import java.util.Arrays;

import edu.cornell.cs4321.IO.Converter;
import org.junit.Test;

import edu.cornell.cs4321.BPlusTree.*;
import edu.cornell.cs4321.Database.DatabaseCatalog;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;


public class testBPlusTree {

	@Test
	public void test() {
		Column c =  new Column();
		Table t = new Table();
		t.setName("Boats");
		c.setTable(t);
		c.setColumnName("D");
		DatabaseCatalog.getInstance("samples/input");
		BPlusTree b = new BPlusTree(true, "Boats", c, 2, "samples/input/db/");
		Converter converter = new Converter("samples/input/db/data/Boats");
		converter.writeToFile("samples/input/db/data/Boats_clustered");
		IndexNode root = b.getRoot();
		System.out.println(Arrays.toString(root.getKeys().toArray()));
		if(root.isUpperLayer())
			bfs(root.getIndexChildren());
	}
	
	public void bfs(ArrayList<IndexNode> input){
		ArrayList<IndexNode> next = new ArrayList<IndexNode>();
		ArrayList<LeafNode> leaves = new ArrayList<LeafNode>();
		for(IndexNode eachNode: input){
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
			for(LeafNode ln : leaves) {
				System.out.print(Arrays.toString(ln.getMap().keySet().toArray())+" ");
			}
		}
		
		if(!next.isEmpty())
			bfs(next);
		else
			return;
	}

}
