package de.batschko.tradeupproject.tradeup;

import de.batschko.tradeupproject.db.query.QRCollection;
import de.batschko.tradeupproject.db.query.QRGenerationSettings;
import de.batschko.tradeupproject.db.query.QRTradeUpGenerated;
import de.batschko.tradeupproject.enums.Condition;
import de.batschko.tradeupproject.enums.Rarity;
import de.batschko.tradeupproject.enums.TradeUpStatus;
import de.batschko.tradeupproject.utils.Utils;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


/**
 * Class to generate TradeUps.
 */
@Slf4j
public class Generator {

    /**
     * Generate all Single-Collection TradeUps for a float dict id.
     *
     * @param floatDictId the float dict id
     */
    public static void generateSingleCollTradeUps(int floatDictId) {
        log.info("Generating Single collection TradeUps....");
        List<TradeUpSettings> coll1All = buildGenerationSettingsSingleColl(Condition.FN);
        coll1All.addAll(buildGenerationSettingsSingleColl(Condition.MW));
        coll1All.addAll(buildGenerationSettingsSingleColl(Condition.FT));

        for (TradeUpSettings tSetting : coll1All) {
            generateTradeUpWrapper(tSetting, floatDictId);
        }
    }

    /**
     * Generate all Double-Collection TradeUps for a float dict id.
     *
     * @param floatDictId the float dict id
     */
    public static void generateDoubleCollTradeUps(int floatDictId) {
        log.info("Generating double collection TradeUps....");
        List<TradeUpSettings> coll1All = generateDoubleCollTradeUpSettings();
        for (TradeUpSettings tSetting : coll1All) {
            generateTradeUpWrapper(tSetting, floatDictId);
        }
    }

    /**
     * Generate all Double-Collection TradeUps for a float dict id using threads.
     *
     * @param numberOfThreads number of threads
     * @param floatDictId the float dict id
     */
    public static void generateDoubleCollTradeUpsWithThreads(int numberOfThreads, int floatDictId) {
        processGenerateDoubleCollTradeUpsWithThreads(numberOfThreads, floatDictId);
    }

    /**
     * Generate a {@link de.batschko.tradeupproject.tables.TradeUp}.
     *
     * @param tSettings   {@link TradeUpSettings}.
     * @param stattrak    stattrak as byte
     * @param rarity      {@link Rarity}.
     * @param floatDictId float dict id
     * @param collCount collection count
     */
    public static void generateTradeUp(TradeUpSettings tSettings,
                                       byte stattrak, Rarity rarity, int floatDictId, byte collCount, int tradeUpSettingsId) {
        TradeUpStatus status = TradeUpStatus.NOT_CALCULATED;
        QRTradeUpGenerated.saveRecord(stattrak, rarity, tSettings.condTarget, collCount, status, floatDictId, tradeUpSettingsId);
    }


    //Wrapper to set Rarities, Stattrak and FloatDict
    private static void generateTradeUpWrapper(TradeUpSettings tSettings,
                                              int floatDictId) {
        // rarities to use as TradeUpSkins -> rarityTarget-1
        List<Rarity> rarities = null;
        int old_length = Integer.MAX_VALUE;
        //only use the lowest common Rarity
        for (String collName : tSettings.collectionList) {
            List<Rarity> rar = Rarity.getPossibleTradeUpRarities(collName);
            if (rar.size() < old_length) {
                rarities = rar;
                old_length = rar.size();
            }

        }
        if (rarities == null) throw new RuntimeException("Can't generate tradeUp, PossibleTradeUpRarityList is null");

        byte collCount = tSettings.getCollectionCount();
        int tradeUpSettingsId = QRGenerationSettings.saveIfNotExists(tSettings.serialize(),false);
        for (Rarity rarity : rarities) {
            // only stattrak for cases
            if (tSettings.hasCollection()) {
                generateTradeUp(tSettings, (byte) 0, rarity, floatDictId, collCount, tradeUpSettingsId);
            } else {
                generateTradeUp(tSettings, (byte) 0, rarity, floatDictId, collCount, tradeUpSettingsId);
                generateTradeUp(tSettings, (byte) 1, rarity, floatDictId, collCount, tradeUpSettingsId);
            }
        }
    }


    private static List<CollectionConditionDistribution> collectionDistributionSingle(Condition c1, Condition c2) {
        List<CollectionConditionDistribution> collectionDistribution = new ArrayList<>();
        for (int firstSlot = 0; firstSlot <= 10; firstSlot++) {
            int secondSlot = 10 - firstSlot;
            collectionDistribution.add(new CollectionConditionDistribution(List.of(firstSlot, secondSlot), List.of(c1, c2)));
        }
        return collectionDistribution;
    }

    private static Set<List<String>> buildCollectionCombinationsSingleColl() {
        List<String> collNames = QRCollection.getAllCollectionNames();
        removeUnwantedColls(collNames);
        Set<List<String>> combinations = new HashSet<>();
        for (String coll : collNames) {
            combinations.add(new ArrayList<>(List.of(coll, coll)));
        }
        return combinations;
    }

    private static Set<List<String>> buildCollectionCombinationsDoubleColl() {
        List<String> collNames = QRCollection.getAllCollectionNames();
        removeUnwantedColls(collNames);
        Set<List<String>> combinations = new HashSet<>();
        for (int i = 0; i < collNames.size(); i++) {
            for (int j = 0; j < collNames.size(); j++) {
                combinations.add(new ArrayList<>(List.of(collNames.get(i), collNames.get(j))));
            }
        }
        return combinations;
    }

    private static void removeUnwantedColls(List<String> collNames) {
        List<String> unwantedColls = QRCollection.getCollectionsUnwanted();
        for(String coll: unwantedColls){
            collNames.remove(coll);
        }
    }

    private static List<TradeUpSettings> buildGenerationSettingsSingleColl(Condition conditionTarget) {
        Condition[] condArray = Condition.getConditionArrayFromTarget(conditionTarget);
        List<CollectionConditionDistribution> collCondDistributions = Generator.collectionDistributionSingle(condArray[0], condArray[1]);
        Set<List<String>> collCombinations = Generator.buildCollectionCombinationsSingleColl();
        List<TradeUpSettings> ccDistri = new ArrayList<>();
        for (List<String> colls : collCombinations) {
            for (CollectionConditionDistribution collCond : collCondDistributions) {
                ccDistri.add(new TradeUpSettings(colls, collCond, conditionTarget));
            }
        }
        return ccDistri;
    }

    private static List<TradeUpSettings> buildGenerationSettingsDoubleColl(Condition conditionTarget) {
        Condition[] condArray = Condition.getConditionArrayFromTarget(conditionTarget);
        List<CollectionConditionDistribution> collCondDistributions = Generator.collectionDistributionSingle(condArray[0], condArray[1]);
        Set<List<String>> collCombinations = Generator.buildCollectionCombinationsDoubleColl();
        List<TradeUpSettings> ccDistri = new ArrayList<>();
        for (List<String> colls : collCombinations) {
            for (CollectionConditionDistribution collCond : collCondDistributions) {
                ccDistri.add(new TradeUpSettings(colls, collCond, conditionTarget));
            }
        }
        return ccDistri;
    }

    private static List<TradeUpSettings> generateDoubleCollTradeUpSettings() {
        log.info("Generating Double collection TradeUpSettings....");
        List<TradeUpSettings> coll1All = buildGenerationSettingsDoubleColl(Condition.FN);
        coll1All.addAll(buildGenerationSettingsDoubleColl(Condition.MW));
        coll1All.addAll(buildGenerationSettingsDoubleColl(Condition.FT));
        return coll1All;
    }

   private static void processGenerateDoubleCollTradeUpsWithThreads(int numberOfThreads, int floatDictId) {
        List<Future<Integer>> futures;
        try (ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads)) {
            List<TradeUpSettings> tSettings = generateDoubleCollTradeUpSettings();
            futures = new ArrayList<>();
            List<List<TradeUpSettings>> parts = Utils.splitList(tSettings, numberOfThreads);
            for (int i = 0; i < numberOfThreads; i++) {
                final int finalI = i;
                Future<Integer> future = executor.submit(() -> generateTradeUpWrapperThread(parts.get(finalI),floatDictId));
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

    private static int generateTradeUpWrapperThread(List<TradeUpSettings> tSettiings, int floatDictId) {
        log.info("Generating TradeUps from {} settings...",tSettiings.size());
        for (TradeUpSettings tSetting : tSettiings) {
            Generator.generateTradeUpWrapper(tSetting, floatDictId);
        }
        return 1;
    }

}