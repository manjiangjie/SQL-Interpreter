package edu.cornell.cs4321.UnionFind;

import java.util.ArrayList;
import java.util.List;

import net.sf.jsqlparser.schema.Column;

/**
 * UnionFind object to store attributes and common values
 * @author Heng
 */
public class UnionFind {
	private List<Element> unionFind;
	
	public UnionFind(){
		unionFind = new ArrayList<Element>();
	}
	
	public List<Element> getUnionFind(){
		return unionFind;
	}
	
	/**
	 * find the element that has that attribute
	 */
	public Element find(Column col){
		
		for(Element e : unionFind){
			if(e.checkColumn(col))
				return e;
		}
		Element newElement = new Element();
		newElement.addColumn(col);
		unionFind.add(newElement);
		return newElement;
	}
	
	/**
	 * merge two union find
	 */
	public void merge(Element e1, Element e2){
		if(e1.equals(e2)) return;
		e1.getAttribute().addAll(e2.getAttribute());
		if(e2.getLowerBound()!=null)
			e1.setLowerBound(e2.getLowerBound());
		if(e2.getUpperBound()!=null)
			e1.setUpperBound(e2.getEquality());
		if(e2.getEquality()!=null)
			e1.setEquality(e2.getEquality());
		unionFind.remove(e2);
	}
	
	/**
	 * update the union element
	 */
	public void setElement(Element e, Long low, Long up, Long equal){
		for(Element element : unionFind){
			if(e.equals(element)){
				if(low!=null)
					element.setLowerBound(low);
				if(up!=null)
					element.setUpperBound(up);
				if(equal!=null)
					element.setEquality(equal);
				return;
			}
		}
	}
	
}
