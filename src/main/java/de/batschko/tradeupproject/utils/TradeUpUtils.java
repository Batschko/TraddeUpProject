package de.batschko.tradeupproject.utils;

import de.batschko.tradeupproject.db.customtable.TradeUpCustom;
import de.batschko.tradeupproject.db.query.QRTradeUpGenerated;
import de.batschko.tradeupproject.tables.CS2Skin;
import de.batschko.tradeupproject.tables.TradeUp;
import de.batschko.tradeupproject.tables.TradeUpSkins;
import de.batschko.tradeupproject.tradeup.TradeUpSettings;
import lombok.extern.slf4j.Slf4j;
import org.jooq.Record;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Utility methods related to {@link TradeUp}.
 */
@Slf4j
public class TradeUpUtils {

    /**
     * Calculates all TradeUps.
     * <p>Gets all TradeUps which are not calculated from db and calculates them</p>
     */
    public static void calculateAllTradeUps(){
        List<TradeUpCustom> tradeUps = QRTradeUpGenerated.getTradeUpsToCalculate();
        log.info("calculating tups: {}", tradeUps.size());
        for(TradeUpCustom tup : tradeUps){
            tup.setCalculation();
        }
    }

    /**
     * Calculates all TradeUps using threads.
     * <p>Gets all TradeUps which are not calculated from db and calculates them</p>
     * @param numberOfThreads number of threads
     */
    public static void calculateAllTradeUpsWithThreads(int numberOfThreads){
        List<Future<Integer>> futures;
        try (ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads)) {
            List<TradeUpCustom> tSettings = QRTradeUpGenerated.getTradeUpsToCalculate();
            futures = new ArrayList<>();
            List<List<TradeUpCustom>> parts = Utils.splitList(tSettings, numberOfThreads);
            for (int i = 0; i < numberOfThreads; i++) {
                final int finalI = i;
                Future<Integer> future = executor.submit(() -> calculateTradeUpsThread(parts.get(finalI)));
                futures.add(future);
            }
            executor.shutdown();
        }

        // Wait for all threads to finish and collect return values
        int totalResult = 0;
        for (Future<Integer> future : futures) {
            try {
                totalResult += future.get(); // This will block until the result is available
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
            log.info("Threads finished {}", totalResult);
        }
    }

    private static int calculateTradeUpsThread(List<TradeUpCustom> tradeUps){
        log.info("calculating tups: {}", tradeUps.size());
        for(TradeUpCustom tup : tradeUps){
            tup.setCalculation();
        }
        return 1;
    }


    /**
     * Re-Calculates all TradeUps.
     * <p>Use this after updating {@link CS2Skin} prices</p>
     */
    public static void reCalculateTradeUps() {
        List<TradeUpCustom> tupList = QRTradeUpGenerated.getTradeUpList();
        TradeUpCustom.reCalculateUpdatedPrices(tupList);
    }

    /**
     * Re-Calculates only calculated TradeUps.
     * <p>Use this after updating {@link CS2Skin} prices</p>
     */
    public static void reCalculateTradeUpsOnlyCalculated() {
        List<TradeUpCustom> tupList = QRTradeUpGenerated.getTradeUpListCalculated();
        TradeUpCustom.reCalculateUpdatedPrices(tupList);
    }


    /**
     * Create {@link TradeUpSkins}.
     * <p>creates all TradeUpSkins for existing TradeUps</p>
     */
    public static void createTradeUpSkins() {
        QRTradeUpGenerated.createTradeUpSkins();
    }

    /**
     * Create {@link TradeUpSkins} with limit.
     *
     * @param limit limit
     */
    public static void createTradeUpSkins(int limit) {
        QRTradeUpGenerated.createTradeUpSkins(limit);
    }

    public static void createTradeUpSkinsWithThreads(int numberOfThreads) {
        List<Future<Integer>> futures;
        try (ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads)) {
            futures = new ArrayList<>();
            List<List<Record>> parts = QRTradeUpGenerated.createTradeUpSkinsWithThreadsSubLists(numberOfThreads);
            for (int i = 0; i < numberOfThreads; i++) {
                final int finalI = i;
                Future<Integer> future = executor.submit(() -> QRTradeUpGenerated.createTradeUpSkinsThread(parts.get(finalI)));
                futures.add(future);
            }
            executor.shutdown();
        }

        // Wait for all threads to finish and collect return values
        int totalResult = 0;
        for (Future<Integer> future : futures) {
            try {
                totalResult += future.get(); // This will block until the result is available
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
            log.info("Threads finished {}", totalResult);
        }
    }

}
