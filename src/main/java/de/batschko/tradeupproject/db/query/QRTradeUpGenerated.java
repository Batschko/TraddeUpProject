package de.batschko.tradeupproject.db.query;

import de.batschko.tradeupproject.db.customtable.TradeUpCustom;
import de.batschko.tradeupproject.enums.Condition;
import de.batschko.tradeupproject.enums.Rarity;
import de.batschko.tradeupproject.enums.TradeUpStatus;
import de.batschko.tradeupproject.tables.TradeUp;
import de.batschko.tradeupproject.tables.TradeUpSkins;
import de.batschko.tradeupproject.tables.records.TradeUpOutcomeRecord;
import de.batschko.tradeupproject.tables.records.TradeUpOutcomeSkinsRecord;
import de.batschko.tradeupproject.tables.records.TradeUpRecord;
import de.batschko.tradeupproject.tradeup.TradeUpSettings;
import de.batschko.tradeupproject.utils.SkinUtils;
import lombok.extern.slf4j.Slf4j;
import org.jooq.Record;
import org.jooq.*;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

import static de.batschko.tradeupproject.tables.TradeUp.TRADE_UP;
import static de.batschko.tradeupproject.tables.TradeUpOutcome.TRADE_UP_OUTCOME;
import static de.batschko.tradeupproject.tables.TradeUpOutcomeSkins.TRADE_UP_OUTCOME_SKINS;
import static de.batschko.tradeupproject.tables.TradeUpSkins.TRADE_UP_SKINS;
import static de.batschko.tradeupproject.tables.VFullcs2skin.V_FULLCS2SKIN;
import static de.batschko.tradeupproject.tables.VTupnsettinggs.V_TUPNSETTINGGS;
import static org.jooq.impl.DSL.min;
import static org.jooq.impl.DSL.row;


/**
 * Database access related to generated {@link TradeUp}
 * <p> ! all methods filter for custom = false ! </p>
 */
@Repository
@Slf4j
public class QRTradeUpGenerated extends QueryRepository{

    public QRTradeUpGenerated(DSLContext dslContext) {
        super(dslContext);
    }

    /**
     * Get all {@link TradeUp}s.
     *
     * @return list of {@link TradeUpCustom}
     */
    public static List<TradeUpCustom> getTradeUpList(){
        return dsl.select()
                .from(TRADE_UP)
                .where(TRADE_UP.CUSTOM.eq((byte) 0))
                .fetchInto(TradeUpCustom.class);
    }

    /**
     * Get all {@link TradeUp}s with limit.
     *
     * @param limit limit
     * @return list of {@link TradeUpCustom}
     */
    public static List<TradeUpCustom> getTradeUpList(int limit){
        return dsl.select()
                .from(TRADE_UP)
                .where(TRADE_UP.CUSTOM.eq((byte) 0))
                .limit(limit)
                .fetchInto(TradeUpCustom.class);
    }

    /**
     * Creates empty {@link TradeUpRecord}.
     *
     * @return  {@link TradeUpRecord}
     */
    public static TradeUpRecord createRecord(){
        return dsl.newRecord(TradeUp.TRADE_UP);
    }

    /**
     * Save {@link TradeUpRecord} with attributes as parameters.
     *
     * @param stattrak          stattrak as byte
     * @param rarity            rarity {@link Rarity}
     * @param condTarget        condition target {@link Condition}
     * @param collCount         collection count
     * @param status            status {@link TradeUpStatus}
     * @param floatDictId       float dict id
     * @param tradeUpSettingsId tradeUp settings id
     */
    public static void saveRecord(byte stattrak, Rarity rarity, Condition condTarget, byte collCount, TradeUpStatus status, int floatDictId, int tradeUpSettingsId){
        TradeUpRecord tup = dsl.newRecord(TRADE_UP);
        tup.setCustom((byte) 0);
        tup.setStattrak(stattrak);
        tup.setRarity(rarity);
        tup.setConditionTarget(condTarget);
        tup.setCollectionCount(collCount);
        tup.setStatus(status);
        tup.setFloatDictId(floatDictId);
        tup.setGenerationSettingsId(tradeUpSettingsId);
        tup.store();
    }

    /**
     * Get {@link TradeUp} by id.
     *
     * @param tupId tradeUp id
     * @return {@link TradeUpCustom}
     */
    public static TradeUpCustom getTradeUp(int tupId){
        return dsl.select()
                .from(TRADE_UP)
                .where(TRADE_UP.ID.eq(tupId))
                .and(TRADE_UP.CUSTOM.eq((byte) 0))
                .fetchOneInto(TradeUpCustom.class);
    }

    /**
     * Get all {@link TradeUp}, which are not calculated.
     *
     * @return list of {@link TradeUpCustom}
     */
    public static List<TradeUpCustom> getTradeUpsToCalculate(){
        List<Integer> ids = dsl.select(TRADE_UP_OUTCOME.TRADEUP_ID)
                .from(TRADE_UP_OUTCOME)
                .where(TRADE_UP_OUTCOME.CUSTOM.eq((byte) 0))
                .fetchInto(Integer.class);
        return dsl.select()
                .from(TRADE_UP)
                .where(TRADE_UP.ID.notIn(ids))
                .and(TRADE_UP.CUSTOM.eq((byte) 0))
                .fetchInto(TradeUpCustom.class);
    }

    /**
     * Create {@link TradeUpSkins}.
     * <p>creates all TradeUpSkins for existing TradeUps</p>
     */
    public static void createTradeUpSkins(){
        Result<Record> tupAndSettings = getTradeUpAndSettingsBuilder().fetch();
        processCreateTradeUpSkins(tupAndSettings);
    }

    /**
     * Create {@link TradeUpSkins} with limit.
     *
     * @param limit limit
     */
    public static void createTradeUpSkins(int limit){
        Result<Record> tupAndSettings = getTradeUpAndSettingsBuilder().limit(limit).fetch();
        processCreateTradeUpSkins(tupAndSettings);
    }

    /**
     * Create {@link Record}(TradeUpAndSettings) sub-lists to use for threads.
     *
     * @param parts parts, number of sub-lists
     * @return the list of sub-lists
     */
    public static List<List<Record>> createTradeUpSkinsWithThreadsSubLists(int parts){
        Result<Record> tupAndSettings = getTradeUpAndSettingsBuilder().fetch();
        List<List<Record>> subResults = new ArrayList<>();
        int chunkSize = (int) Math.ceil((double) tupAndSettings.size() / parts);
        for (int i = 0; i < tupAndSettings.size(); i += chunkSize) {
            subResults.add(tupAndSettings.subList(i, Math.min(tupAndSettings.size(), i + chunkSize)));
        }
        return subResults;
    }

    /**
     * Thread method to create {@link TradeUpSkins}.
     */
    public static int createTradeUpSkinsThread(Iterable<Record> tupAndSettings){
        return processCreateTradeUpSkins(tupAndSettings);
    }

    private static SelectConditionStep<Record> getTradeUpAndSettingsBuilder(){
        return dsl.select()
                .from(V_TUPNSETTINGGS)
                .leftJoin(TRADE_UP_SKINS)
                .on(V_TUPNSETTINGGS.ID.eq(TRADE_UP_SKINS.TRADE_UP_ID).and(V_TUPNSETTINGGS.CUSTOM.eq(TRADE_UP_SKINS.CUSTOM)))
                .where(TRADE_UP_SKINS.TRADE_UP_ID.isNull())
                .and(V_TUPNSETTINGGS.CUSTOM.eq((byte) 0));
    }

    /**
     * Update {@link TradeUpSkins}.
     * <p>create new tradeUp skins for a calculated TradeUp</p>
     *
     * @param tupId tradeUp id
     */
    public static void updateTradeUpSkins(int tupId){
        dsl.deleteFrom(TRADE_UP_SKINS).where(TRADE_UP_SKINS.TRADE_UP_ID.eq(tupId)).and(TRADE_UP_SKINS.CUSTOM.eq((byte) 0)).execute();
        Result<Record> tupAndSettings = dsl.select().from(V_TUPNSETTINGGS).where(V_TUPNSETTINGGS.ID.eq(tupId)).and(V_TUPNSETTINGGS.CUSTOM.eq((byte) 0)).fetch();
        processCreateTradeUpSkins(tupAndSettings);
    }

    /**
     * Get {@link TradeUpOutcomeRecord}.
     *
     * @param tupId tradeUp id
     * @return tradeUp outcome record
     */
    public static TradeUpOutcomeRecord getTradeUpOutcome(int tupId){
        return dsl.select().from(TRADE_UP_OUTCOME).where(TRADE_UP_OUTCOME.TRADEUP_ID.eq(tupId)).and(TRADE_UP_OUTCOME.CUSTOM.eq((byte) 0)).fetchOneInto(TradeUpOutcomeRecord.class);
    }

    /**
     * Get list of {@link TradeUpOutcomeSkinsRecord}.
     *
     * @param tupId tradeUp id
     * @return list of outcome skin records
     */
    public static List<TradeUpOutcomeSkinsRecord> getTradeUpOutcomeSkins(int tupId){
        return dsl.select().from(TRADE_UP_OUTCOME_SKINS).where(TRADE_UP_OUTCOME_SKINS.TRADE_UP_ID.eq(tupId)).and(TRADE_UP_OUTCOME_SKINS.CUSTOM.eq((byte) 0)).fetchInto(TradeUpOutcomeSkinsRecord.class);
    }
    
    private static int processCreateTradeUpSkins(Iterable<Record> tupAndSettings){
        for(Record tupAndS: tupAndSettings){
            byte stat = tupAndS.get("stattrak", Byte.class);
            Rarity rarity = tupAndS.get("rarity", Rarity.class);
            int tupId = tupAndS.get("id", Integer.class);

            TradeUpSettings settings = TradeUpSettings.deserialize(tupAndS.get("settings", String.class));
            List<SkinUtils.TradeUpSkinInfo> infos = settings.getTradeUpSkinInfo(rarity, stat);
            for(SkinUtils.TradeUpSkinInfo info : infos){
                SelectConditionStep<Record1<Double>> subquery = dsl.select(min(V_FULLCS2SKIN.PRICE.mul(1.15)))
                        .from(V_FULLCS2SKIN)
                        .where(V_FULLCS2SKIN.COLL_NAME.eq(info.getColl_name()))
                        .and(V_FULLCS2SKIN.RARITY.eq(info.getRarity()))
                        .and(V_FULLCS2SKIN.CONDITION.eq(info.getCondition()))
                        .and(V_FULLCS2SKIN.STATTRAK.eq(info.getStattrak()))
                        .and(V_FULLCS2SKIN.PRICE.gt(0.0));


                SelectConditionStep<Record1<Integer>> query = dsl.select(V_FULLCS2SKIN.ID)
                        .from(V_FULLCS2SKIN)
                        .where(V_FULLCS2SKIN.COLL_NAME.eq(info.getColl_name()))
                        .and(V_FULLCS2SKIN.RARITY.eq(info.getRarity()))
                        .and(V_FULLCS2SKIN.CONDITION.eq(info.getCondition()))
                        .and(V_FULLCS2SKIN.STATTRAK.eq(info.getStattrak()))
                        .and(V_FULLCS2SKIN.PRICE.ge(0.0))
                        .and(V_FULLCS2SKIN.PRICE.le(subquery));

                List<Integer> tupSkinIds = query.fetchInto(Integer.class);
                //TODO delete tradeups that are not possible because condition doesnt exist?
                List<Row3<Byte, Integer, Integer>> rowList = tupSkinIds.stream().map(tupSkinId -> row((byte)0 ,tupId, tupSkinId)).toList();
                if(rowList.isEmpty()){
                    log.warn(""+settings + " "+rarity+ " "+ stat);
                }

                try{
                    int inserted = dsl.insertInto(TRADE_UP_SKINS, TRADE_UP_SKINS.CUSTOM, TRADE_UP_SKINS.TRADE_UP_ID, TRADE_UP_SKINS.C_S2_SKIN_ID).valuesOfRows(rowList).execute();
                    if(inserted != tupSkinIds.size()){
                        throw new RuntimeException("Error inserting TradeUpSkins");
                    }
                }catch (Exception e){
                    throw new RuntimeException("Exception inserting TradeUpSkins");
                }
            }
        }
        return 1;
    }


    /**
     * Reset {@link TradeUpStatus} for all {@link TradeUp}s.
     *<p>->dangerous!<-</p>
     * @return the number of updated records
     */
    public static int resetTradeUpStatusAll(){
        return dsl.update(TRADE_UP).set(TRADE_UP.STATUS, TradeUpStatus.NOT_CALCULATED).where(TRADE_UP.CUSTOM.eq((byte) 0)).execute();
    }


    /**
     * Update {@link TradeUp} status ({@link TradeUpStatus}) by id.
     *
     * @param id            tradeUp id
     * @param tradeUpStatus tradeUp status
     */
    public static void updateStatus(int id, TradeUpStatus tradeUpStatus) {
       dsl.update(TRADE_UP).set(TRADE_UP.STATUS, tradeUpStatus).where(TRADE_UP.ID.eq(id)).and(TRADE_UP.CUSTOM.eq((byte) 0)).execute();
    }
}
