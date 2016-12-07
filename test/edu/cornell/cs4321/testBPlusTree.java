package edu.cornell.cs4321;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import edu.cornell.cs4321.IO.Converter;
import org.junit.Test;

import edu.cornell.cs4321.BPlusTree.*;
import edu.cornell.cs4321.Database.DatabaseCatalog;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;


public class testBPlusTree {

	@Test
	public void test() {
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
			System.out.println();
		}
		
		if(!next.isEmpty())
			bfs(next);
		else
			return;
	}
	
	

}
