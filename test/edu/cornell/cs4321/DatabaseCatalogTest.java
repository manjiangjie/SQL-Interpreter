package edu.cornell.cs4321;

import static org.junit.Assert.*;

import org.junit.Test;

import edu.cornell.cs4321.Database.DatabaseCatalog;

public class DatabaseCatalogTest {

	@Test
	public void test() {
		DatabaseCatalog.getInstance("samples/2/input");
		double rf = DatabaseCatalog.getReductionFactor("Reserves", "G", null, null, null, null);
		double rf1 = DatabaseCatalog.getReductionFactor("Reserves", "G", (Long)1000L, null, (Boolean)false, null);
		double rf2 = DatabaseCatalog.getReductionFactor("Reserves", "G", null, (Long)2000L, null, (Boolean)true);
		double rf3 = DatabaseCatalog.getReductionFactorClosed("Reserves", "G", (Long)1000L, null);
		System.out.print(rf+"\n"+rf1+"\n"+rf2+"\n"+rf3+"\n");
		int n = DatabaseCatalog.getNumLeaves("Reserves", "G");
		int p = DatabaseCatalog.getNumPages("Reserves");
		System.out.println(n + "\n" + p);
	}

}
