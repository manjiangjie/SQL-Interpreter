package edu.cornell.cs4321.IO;

import edu.cornell.cs4321.Database.DatabaseCatalog;
import edu.cornell.cs4321.Database.Tuple;
import net.sf.jsqlparser.schema.Column;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

/**
 * Created by manjiangjie on 10/14/16.
 */
public class StandardTupleReader implements TupleReader {
    private String tablePath;
    private List<Column> schemaList;
    private FileReader fr;
    private BufferedReader br;

    public StandardTupleReader(String tableName) {
        schemaList = DatabaseCatalog.getSchemaByTable(tableName);
        tablePath = DatabaseCatalog.getPathByTableName(tableName);
        try {
            fr = new FileReader(tablePath);
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            System.out.println("Table not found! Please check your input");
            fr = null;
        }
        //Initialize and buffered reader
        if(fr != null){
            br = new BufferedReader(fr);
        }
    }

    /**
     * This method read the next tuple in the table data file.
     *
     * @return the next tuple
     */
    @Override
    public Tuple readNextTuple() {
        try {
            String record = br.readLine();
            if(record == null) {
                return null;
            }
            //System.out.println("!!!"+record);
            return new Tuple(schemaList, record);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Close the input stream.
     */
    @Override
    public void close() {
        try {
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * For debugging use.
     * Print all tuples retrieved to Console.
     */
    @Override
    public void dump() {
        Tuple t;
        while((t = readNextTuple()) != null){
            System.out.println(t);
        }
    }

    /**
     * re-generate input stream and re-read the data file
     */
    @Override
    public void reset() {
        try {
            fr = new FileReader(tablePath);
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            System.out.println("Table not found! Please check your input");
            fr = null;
        }
        br = new BufferedReader(fr);
    }
}
