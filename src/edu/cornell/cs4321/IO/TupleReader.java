package edu.cornell.cs4321.IO;

import edu.cornell.cs4321.Database.Tuple;

/**
 * Created by manjiangjie on 10/12/16.
 */
public interface TupleReader {
    Tuple readNextTuple();

    void close();

    void reset();
}
