package edu.cornell.cs4321.IO;

import edu.cornell.cs4321.Database.Tuple;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.List;

/**
 * This class implements TupleWriter, which writes a Tuple in binary format to output file.
 * @author Jiangjie Man: jm2559
 */
public class BinaryTupleWriter implements TupleWriter {
    private FileOutputStream fout;
    private FileChannel fc;
    private ByteBuffer bb;
    private int SIZE = 4096;
    private int index = 0;
    private int numAttributes;
    private int numTuples;

    /**
     * Constructor for BinaryTupleWriter class. Initiate FileOutputStream and ByteBuffer.
     * @param queryPath a string for the path of the output file.
     */
    public BinaryTupleWriter(String queryPath) {
        try {
            fout = new FileOutputStream(queryPath);
            fc = fout.getChannel();
            bb = ByteBuffer.allocate(SIZE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Write a Tuple in binary format. If tuples are fit in a page, write to next page.
     * @param t The Tuple Object to be written in.
     */
    @Override
    public void writeNextTuple(Tuple t) {
        if (index == 0) {
            numAttributes = t.getRecord().split(",").length;
            numTuples = (SIZE / 4 - 2) / numAttributes;
            bb.putInt(numAttributes);
            bb.putInt(numTuples);
            index += 8;
        }

        List<Integer> attributes = t.getValues();
        //System.out.println(attributes);
        for (int i = 0; i < numAttributes; i++) {
            bb.putInt(attributes.get(i));
            index += 4;
        }

        if (index == 4 * (2 + numTuples * numAttributes)) {
            // padding with zeros
            while (index < SIZE) {
                bb.putInt(0);
                index += 4;
            }
            // write to the buffer
            try {
                bb.flip();
                fc.write(bb);
                if (bb.hasRemaining()) {
                    bb.compact();
                } else {
                    bb.clear();
                }
                index = 0;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Close the output stream.
     */
    @Override
    public void close() {
        try {
            // fill last page
            if (index != 0) {
                while (index < SIZE) {
                    bb.putInt(0);
                    index += 4;
                }
                bb.flip();
                fc.write(bb);
                if (bb.hasRemaining()) {
                    bb.compact();
                    bb.flip();
                    fc.write(bb);
                }
            }
            // close output stream
            fout.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
