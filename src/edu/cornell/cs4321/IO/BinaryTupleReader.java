package edu.cornell.cs4321.IO;

import edu.cornell.cs4321.Database.DatabaseCatalog;
import edu.cornell.cs4321.Database.Tuple;
import net.sf.jsqlparser.schema.Column;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.List;

/**
 * This class implements TupleReader, using the new binary data file format for input.
 * @author Jiangjie Man: jm2559
 */
public class BinaryTupleReader implements TupleReader {
    private String tablePath;
    private List<Column> schemaList;
    private int size = 4096;
    private FileChannel fc;
    private ByteBuffer bb;
    private int index = 0;
    private int numAttributes = 0;
    private int numTuples = 0;
    private FileInputStream fin;

    /**
     * Construct a binaryTupleReader by table
     * @param tableName the table to be read in
     */
    public BinaryTupleReader(String tableName) {
        tablePath = DatabaseCatalog.getPathByTableName(tableName);
        schemaList = DatabaseCatalog.getSchemaByTable(tableName);
        try {
            fin = new FileInputStream(tablePath);
            fc = fin.getChannel();
            bb = ByteBuffer.allocate(size);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Read the next tuple from binary file
     * @return the next tuple
     */
    @Override
    public Tuple readNextTuple() {
        String record = "";
        if (index == 4 * (2 + numAttributes * numTuples) || index == 0) {
            try {
                bb.clear();
                int r = fc.read(bb);
                if (r == -1) {
                    return null;
                }
                index = 0;
                numAttributes = bb.getInt(index);
                numTuples = bb.getInt(index + 4);
                index += 8;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (index < 4 * (2 + numAttributes * numTuples)) {
            for (int i = 0; i < numAttributes; i++) {
                int value = bb.getInt(index);
                record += Integer.toString(value) + ",";
                index += 4;
            }
        }
        record = record.substring(0, record.length() - 1);
        return new Tuple(schemaList, record);
    }

    /**
     * Close the input stream.
     */
    @Override
    public void close() {
        try {
            fin.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Reset the input stream and buffer. Re-read the table data file.
     */
    @Override
    public void reset() {
        try {
            fin.close();
            FileInputStream fin = new FileInputStream(tablePath);
            fc = fin.getChannel();
            bb = ByteBuffer.allocate(size);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * For debugging use. Print all the tuples from this table
     */
    @Override
    public void dump() {
        Tuple t;
        while((t = readNextTuple()) != null){
            System.out.println(t);
        }
    }
}
