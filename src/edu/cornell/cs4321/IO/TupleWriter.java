package edu.cornell.cs4321.IO;

import edu.cornell.cs4321.Database.Tuple;

/**
 * This class is the interface for writing a Tuple in binary format to output file.
 * @author Jiangjie Man: jm2559.
 */
public interface TupleWriter {

    /**
     * Write a Tuple to file. If tuples are fit in a page, write to next page.
     * @param t The Tuple Object to be written in.
     * @param t
     */
    void writeNextTuple(Tuple t);

    /**
     * Close the output stream.
     */
    void close();

}
