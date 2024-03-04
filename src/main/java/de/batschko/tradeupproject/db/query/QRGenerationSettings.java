package de.batschko.tradeupproject.db.query;


import de.batschko.tradeupproject.tables.GenerationSettings;
import de.batschko.tradeupproject.tables.records.GenerationSettingsRecord;
import de.batschko.tradeupproject.tradeup.TradeUpSettings;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import static de.batschko.tradeupproject.tables.GenerationSettings.GENERATION_SETTINGS;

/**
 * Database access related to {@link GenerationSettings} / {@link TradeUpSettings}.
 */
@Repository
public class QRGenerationSettings extends QueryRepository {

    public QRGenerationSettings(DSLContext dslContext) {
        super(dslContext);
    }



    /**
     * Get {@link TradeUpSettings} by id.
     *
     * @param tradeUpSettingsId tradeup settings id
     * @return {@link TradeUpSettings}
     */
    public static TradeUpSettings getTradeUpSettings(int tradeUpSettingsId) {
            String result = dsl.select(GENERATION_SETTINGS.SETTINGS)
                    .from(GENERATION_SETTINGS)
                    .where(GENERATION_SETTINGS.ID.eq(tradeUpSettingsId))
                    .fetchOneInto(String.class);
            if(result == null) throw new RuntimeException("Couldn't query TradeUpSettings with id: " + tradeUpSettingsId);

            return TradeUpSettings.deserialize(result);
    }

    /**
     * Save serialized {@link TradeUpSettings} as {@link GenerationSettings} if no entry exists.
     *
     * @param settings  serialized {@link TradeUpSettings}
     * @return id for the existing/saved settings
     */
    public static int saveIfNotExists(String settings) {

        Integer existingRecord = dsl.select(GENERATION_SETTINGS.ID)
                .from(GENERATION_SETTINGS)
                .where(GENERATION_SETTINGS.SETTINGS.eq(settings))
                .fetchOneInto(Integer.class);
        if (existingRecord == null) {
            GenerationSettingsRecord test_obj = dsl.newRecord(GenerationSettings.GENERATION_SETTINGS);
            test_obj.setSettings(settings);
            test_obj.store();
            return test_obj.getId();
        }
        return existingRecord;


    }
}