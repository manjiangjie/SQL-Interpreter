package edu.cornell.cs4321.IO;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.LinkedList;
import java.util.Queue;

/**
 * A converter which reads from binary file and then writes to human-readable file.
 * @author Jiangjie Man: jm2559
 */
public class Converter {
    private FileInputStream fin;
    private FileChannel fc;
    private ByteBuffer bb;
    private int SIZE = 4096;
    private PrintWriter pw;
    private int numAttributes = 0;
    private int numTuples = 0;
    private int index = 0;
    private Queue<String> records = new LinkedList<>();

    /**
     * Constructor for Converter.
     * @param queryPath the file path to be read in.
     */
    public Converter(String queryPath) {
        try {
            fin = new FileInputStream(queryPath);
            fc = fin.getChannel();
            bb = ByteBuffer.allocate(SIZE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Write the binary format file into human-readable format file.
     * @param fileName The file name to be written in.
     */
    public void writeToFile(String fileName) {
        try {
            pw = new PrintWriter(fileName);
            String record = readTuple();
            while (record != null) {
                pw.println(record);
                record = readTuple();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            pw.close();
        }
    }

    /**
     * A helper method for reading binary file.
     * @return A string to be output each line.
     */
    private String readTuple() {
        String record = "";
        if (records.isEmpty()) {
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
                for (int i = 0; i < numTuples; i++) {
                    for (int j = 0; j < numAttributes; j++) {
                        int value = bb.getInt(index);
                        record += Integer.toString(value) + ",";
                        index += 4;
                    }
                    records.add(record);
                    record = "";
                }
                index = 8;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        record = records.poll();
        index += 4;
        record = record.substring(0, record.length() - 1);
        return record;
    }
}
