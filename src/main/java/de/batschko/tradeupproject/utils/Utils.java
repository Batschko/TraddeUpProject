package de.batschko.tradeupproject.utils;


import lombok.extern.slf4j.Slf4j;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
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
            log.error("Error writing test file"+e.getMessage());
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
            log.error("Error writing test file"+e.getMessage());
        }
    }

    /**
     * Splits a list into multiple parts.
     *
     * @param <T> the type of elements in the list
     * @param list the list to be split
     * @param parts the number of parts to split the list into
     * @return a list of sublists
     * @throws IllegalArgumentException if parts is less than or equal to 0 or greater than the size of the list
     */
    public static <T> List<List<T>> splitList(List<T> list, int parts) {
        if (parts <= 0 || parts > list.size()) {
            throw new IllegalArgumentException("Invalid number of parts: " + parts);
        }

        List<List<T>> subLists = new ArrayList<>();
        int chunkSize = (int) Math.ceil((double) list.size() / parts);

        for (int i = 0; i < list.size(); i += chunkSize) {
            subLists.add(new ArrayList<>(list.subList(i, Math.min(list.size(), i + chunkSize))));
        }

        return subLists;
    }
}
