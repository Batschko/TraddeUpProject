package de.batschko.tradeupproject.utils;


import lombok.extern.slf4j.Slf4j;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 * Utility methods that do not belong to any specific file.
 */
@Slf4j
public class Utils {


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
            log.warn("An error occurred.");
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
            log.warn("An error occurred.");
            e.printStackTrace();
        }
    }
}
