package edu.cornell.cs4321.IO;

import edu.cornell.cs4321.Database.Tuple;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * This class generates random tuples from stats.txt, with the statistical properties described in that file.
 *
 * @author Jiangjie Man: jm2559
 */
public class DataGenerator {

    public DataGenerator(String inputDir) {
        try {
            FileReader fr = new FileReader(inputDir + "/db/stats.txt");
            BufferedReader br = new BufferedReader(fr);
            String currentLine;
            while ((currentLine = br.readLine()) != null) {
                //System.out.println(currentLine);
                String[] tokens = currentLine.split("\\s+");
                String tableName = tokens[0];
                Table table = new Table();
                table.setName(tableName);
                int numTuples = Integer.parseInt(tokens[1]);
                int[][] range = new int[tokens.length - 2][2];
                List<Column> columns = new ArrayList<>();
                for (int i = 2; i < tokens.length; i++) {
                    String[] column = tokens[i].split(",");
                    columns.add(new Column(table, column[0]));
                    range[i - 2][0] = Integer.parseInt(column[1]);
                    range[i - 2][1] = Integer.parseInt(column[2]);
                }
                String filePath = inputDir + "/db/data/" + tableName + "_random";
                BinaryTupleWriter btw = new BinaryTupleWriter(filePath);
                while (numTuples > 0) {
                    numTuples -= 1;
                    Tuple t = getRandomTuple(range, columns);
                    btw.writeNextTuple(t);
                }
                btw.close();
                Converter converter = new Converter(filePath);
                converter.writeToFile(filePath + "_humanreadable");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Generate random numbers in a tuple.
     * @param range Specific range for attribute values
     * @param columns the columns for constructing new tuple
     * @return tuple
     */
    private Tuple getRandomTuple(int[][] range, List<Column> columns) {
        String record = "";
        for (int i = 0; i < range.length; i++) {
            int min = range[i][0];
            int max = range[i][1];
            int random = min + (int) (Math.random() * (max - min + 1));
            record += Integer.toString(random);
            if (i < range.length - 1) {
                record += ",";
            }
        }
        return new Tuple(columns, record);
    }
}
