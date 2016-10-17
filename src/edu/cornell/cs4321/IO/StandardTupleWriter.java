package edu.cornell.cs4321.IO;

import edu.cornell.cs4321.Database.Tuple;

import java.io.IOException;
import java.io.PrintWriter;

/**
 * This class implements TupleWriter interface, which writes Tuples in human-readable format.
 * @author Jiangjie Man: jm2559.
 */
public class StandardTupleWriter implements TupleWriter {
    private PrintWriter pw;

    /**
     * Constructor for StandardTupleWriter, create a PrintWriter Object.
     * @param queryPath the file path to be written
     */
    public StandardTupleWriter(String queryPath) {
        try {
            pw = new PrintWriter(queryPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Write a Tuple to file. If tuples are fit in a page, write to next page.
     * @param t The Tuple Object to be written in.
     */
    @Override
    public void writeNextTuple(Tuple t) {
        pw.println(t.getRecord());
    }

    /**
     * Close the output stream.
     */
    @Override
    public void close() {
        pw.close();
    }
}
