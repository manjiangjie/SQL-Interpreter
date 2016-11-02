package edu.cornell.cs4321.IO;

import edu.cornell.cs4321.Database.DatabaseCatalog;
import edu.cornell.cs4321.Database.Tuple;
import net.sf.jsqlparser.schema.Column;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

/**
 * This class implements TupleReader interface, which reads Tuples in human-readable format.
 * @author Jiangjie Man: jm2559
 */
public class StandardTupleReader implements TupleReader {
    private String tablePath;
    private List<Column> schemaList;
    private FileReader fr;
    private BufferedReader br;

    /**
     * Constructor for StandardTupleReader.
     * @param tableName The table to be read in
     */
    public StandardTupleReader(String tableName) {
        schemaList = DatabaseCatalog.getSchemaByTable(tableName);
        tablePath = DatabaseCatalog.getPathByTableName(tableName);
        try {
            fr = new FileReader(tablePath);
        } catch (FileNotFoundException e) {
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
     * Constructor for StandardTupleReader.
     * @param file: file to be read; schemaList the schema list of the file
     */
    public StandardTupleReader(File file, List<Column> schemaList) {
        this.schemaList = schemaList;
        tablePath = file.getAbsolutePath();
        try {
            fr = new FileReader(tablePath);
        } catch (FileNotFoundException e) {
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
     * Read the next tuple from readable file 
     * without changing the elements in the buffered reader.
     * @return the next tuple
     */
    public Tuple peek(){
    	try {
    		br.mark(1000);
            String record = br.readLine();
            br.reset();
            if(record == null) {
                return null;
            }
            return new Tuple(schemaList, record);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * delete the file that is being read
     * and close the buffer reader
     * **/
    public void deleteFile(){	
    	File readingFile = new File(tablePath);
    	readingFile.delete();
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
     * re-generate input stream and re-read the data file.
     */
    @Override
    public void reset() {
        try {
            fr = new FileReader(tablePath);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.out.println("Table not found! Please check your input");
            fr = null;
        }
        br = new BufferedReader(fr);
    }
    
    public void reset(int index) {
    	
    }
}
