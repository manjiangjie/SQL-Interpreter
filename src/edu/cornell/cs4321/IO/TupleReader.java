package edu.cornell.cs4321.IO;

import edu.cornell.cs4321.Database.Tuple;

/**
 * The TupleReader Interface for data input
 * @author Jiangjie Man: jm2559
 */
public interface TupleReader {

    /**
     * This method read the next tuple in the table data file.
     * @return the next tuple
     */
    Tuple readNextTuple();

    /**
     * re-generate input stream and re-read the data file
     */
    void reset();

    /**
     * For debugging use.
     * Print all tuples retrieved to Console.
     */
    void dump();
}
