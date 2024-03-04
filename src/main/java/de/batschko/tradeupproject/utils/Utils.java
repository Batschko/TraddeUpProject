package de.batschko.tradeupproject.utils;


import de.batschko.tradeupproject.db.customtable.TradeUpCustom;
import de.batschko.tradeupproject.db.query.QRTradeUp;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 * Utility methods that do not belong to any specific file.
 */
public class Utils {


    /**
     * Calculates all trade ups.
     * <p>Gets all TradeUps which are not calculated from db and calculates them</p>
     */
    public static void calculateAllTradeUps(){
        List<TradeUpCustom> tradeUps = QRTradeUp.getTradeUpsToCalculate();
        for(TradeUpCustom tup : tradeUps){
            tup.setCalculation();
        }
    }


    /**
     * Write test file from string.
     * <p>overwrites existing files</p>
     * @param text     the text
     * @param fileName the file name (overwrites)
     */
    public static void writeTestFile(String text, String fileName){
        try {
            BufferedWriter output = new BufferedWriter(new FileWriter("temp/"+fileName+".txt"));
            output.write(text);
            output.close();
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }

    /**
     * Write test file from list of strings.
     * <p>overwrites existing files</p>
     * @param text     list of text strings
     * @param fileName the file name (overwrites)
     */
    public static void writeTestFile(List<String> text, String fileName){
        try {
            BufferedWriter output = new BufferedWriter(new FileWriter("temp/"+fileName+".txt"));
            output.write(String.join("\n", text));
            output.close();
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }
}
