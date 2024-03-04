package de.batschko.tradeupproject.db.query;

import de.batschko.tradeupproject.db.customtable.CS2SkinCustom;
import de.batschko.tradeupproject.enums.CaseCollection;
import de.batschko.tradeupproject.tables.Collection;
import de.batschko.tradeupproject.tables.records.CollectionRecord;
import de.batschko.tradeupproject.tables.records.StashSkinHolderRecord;
import de.batschko.tradeupproject.utils.SkinUtils;
import de.batschko.tradeupproject.webfetchers.StashScraper;
import lombok.extern.slf4j.Slf4j;
import org.jooq.Batch;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

import static de.batschko.tradeupproject.tables.Collection.COLLECTION;

/**
 * Database access for initial queries.
 */
@Slf4j
@Repository
public class QRInitQueries extends QueryRepository{

    public QRInitQueries(DSLContext dslContext) {
        super(dslContext);
    }


    /**
     * Initialize {@link Collection} table with all cases and collections.
     */
    public static void loadInitialCaseCollection() {

        Record record = dsl.selectFrom(COLLECTION).fetchAny();
        if (record != null) {
            log.info("Collection table already initialized");
            return;
        }

        Map<String, String> caseMap = StashScraper.getCaseCollectionNamesAndUrls(CaseCollection.CASE);
        for (String ccName : caseMap.keySet()) {
            CollectionRecord ccRecord = dsl.newRecord(COLLECTION);
            ccRecord.setCollName(ccName);
            ccRecord.setIsCase((byte) 1);
            ccRecord.store();
        }
        Map<String, String> collMap = StashScraper.getCaseCollectionNamesAndUrls(CaseCollection.COLLECTION);
        for (String ccName : collMap.keySet()) {
            CollectionRecord ccRecord = dsl.newRecord(COLLECTION);
            ccRecord.setCollName(ccName);
            ccRecord.setIsCase((byte) 0);
            ccRecord.store();
        }
    }

    /**
     * Generate all {@link StashSkinHolderRecord} and save them to db.
     */
    public static void generateAndSaveStashHolderToDatabase(){
        List<StashSkinHolderRecord> recordList = StashScraper.generateStashSkinHolderListAll();
            Batch batch = dsl.batchMerge(recordList);
            batch.execute();
    }


    /**
     * Generate all {@link CS2SkinCustom} and save them to db.
     */
    public static void generateAndSaveCS2SkinToDatabase(){
        List<CS2SkinCustom> skinList = SkinUtils.generateCS2Skins();
        Batch batch = dsl.batchMerge(skinList);
        batch.execute();
    }

}
