package de.batschko.tradeupproject.db.query;

import de.batschko.tradeupproject.enums.Condition;
import de.batschko.tradeupproject.enums.Rarity;
import de.batschko.tradeupproject.enums.TradeUpStatus;
import de.batschko.tradeupproject.tables.TradeUp;
import de.batschko.tradeupproject.tables.TradeUpSkins;
import de.batschko.tradeupproject.tables.records.TradeUpRecord;
import de.batschko.tradeupproject.tradeup.TradeUpSettings;
import de.batschko.tradeupproject.utils.SkinUtils;
import lombok.extern.slf4j.Slf4j;
import org.jooq.DSLContext;
import org.jooq.Record1;
import org.jooq.Row3;
import org.jooq.SelectConditionStep;
import org.springframework.stereotype.Repository;

import java.util.List;

import static de.batschko.tradeupproject.tables.GenerationSettings.GENERATION_SETTINGS;
import static de.batschko.tradeupproject.tables.TradeUp.TRADE_UP;
import static de.batschko.tradeupproject.tables.TradeUpSkins.TRADE_UP_SKINS;
import static de.batschko.tradeupproject.tables.VFullcs2skin.V_FULLCS2SKIN;
import static org.jooq.impl.DSL.min;
import static org.jooq.impl.DSL.row;

/**
 * Database access related to custom {@link TradeUp}
 * ! all methods filter for custom = true !
 */
@Repository
@Slf4j
public class QRTradeUpCustom extends QueryRepository{

    public QRTradeUpCustom(DSLContext dslContext) {
        super(dslContext);
    }

    /**
     * Save custom {@link TradeUp} to database.
     *
     * @param rarity  rarity
     * @param stat  stattrak
     * @param floatDictId  float dict id
     * @param collCount  collCount
     * @param condTarget  condTarget
     * @param gSettingsId  gSettingsId
     */
    public static void createTradeUpCustom(Rarity rarity, boolean stat, byte collCount, Condition condTarget, int floatDictId, int gSettingsId){
       TradeUpRecord tup = dsl.newRecord(TRADE_UP);
       tup.setCustom((byte) 1);
       tup.setStattrak((byte) (stat?1:0));
       tup.setRarity(rarity);
       tup.setConditionTarget(condTarget);
       tup.setCollectionCount(collCount);
       tup.setStatus(TradeUpStatus.CALCULATED);
       tup.setFloatDictId(floatDictId);
       tup.setGenerationSettingsId(gSettingsId);
       tup.store();
    }

    /**
     * Set {@link TradeUp} floatSumNeeded.
     *
     * @param tupId    tradeUp id
     * @param floatSum float sum
     */
    public static void setFloatSum(int tupId, double floatSum){
        dsl.update(TRADE_UP).set(TRADE_UP.FLOAT_SUM_NEEDED, floatSum).where(TRADE_UP.ID.eq(tupId)).and(TRADE_UP.CUSTOM.eq((byte)1)).execute();
    }

    /**
     * Delete custom {@link TradeUp}.
     *
     * @param tupId tradeUp id
     */
    public static void delete(int tupId){
        dsl.delete(TRADE_UP).where(TRADE_UP.CUSTOM.eq((byte)1)).and(TRADE_UP.ID.eq(tupId)).execute();
    }

    /**
     * Get dummy {@link TradeUp} id.
     *
     * @return dummy tradeUp id
     */
    public static int getDummyId(){
        Integer id = dsl.select(TRADE_UP.ID).from(TRADE_UP).where(TRADE_UP.CUSTOM.eq((byte)1)).and(TRADE_UP.ID.eq(-5)).fetchOneInto(Integer.class);
        if(id == null){
            Integer gId = dsl.select(GENERATION_SETTINGS.ID).from(GENERATION_SETTINGS).where(GENERATION_SETTINGS.CUSTOM.eq((byte)1)).and(GENERATION_SETTINGS.ID.eq(-5)).fetchOneInto(Integer.class);
            if(gId == null){
                dsl.insertInto(GENERATION_SETTINGS).set(GENERATION_SETTINGS.CUSTOM, (byte)1).set(GENERATION_SETTINGS.SETTINGS, "dummy").execute();
                Integer tmpId = dsl.select(GENERATION_SETTINGS.ID).from(GENERATION_SETTINGS).where(GENERATION_SETTINGS.SETTINGS.eq("dummy")).fetchSingleInto(Integer.class);
                dsl.update(GENERATION_SETTINGS).set(GENERATION_SETTINGS.ID, -5).where(GENERATION_SETTINGS.CUSTOM.eq((byte)1).and(GENERATION_SETTINGS.ID.eq(tmpId))).execute();
            }
            createTradeUpCustom(Rarity.HOWL, false, (byte) 0, Condition.BS, 2,-5);
            int nextTupId = QRUtils.getNextCustomTradeUpId()-1;
            dsl.update(TRADE_UP).set(TRADE_UP.ID, -5).where(TRADE_UP.CUSTOM.eq((byte)1).and(TRADE_UP.ID.eq(nextTupId))).execute();
        }
        return -5;
    }

    /**
     * Create custom {@link TradeUpSkins} for custom {@link TradeUp}.
     *
     * @param tupId    tradeUp id
     * @param settings tradeUpSettings
     * @param rarity   rarity
     * @param stat     stattrak
     */
    public static void createTradeUpSkinsCustom(int tupId, TradeUpSettings settings, Rarity rarity, byte stat){
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

            List<Row3<Byte, Integer, Integer>> rowList = tupSkinIds.stream().map(tupSkinId -> row((byte)1 ,tupId, tupSkinId)).toList();
            if(rowList.isEmpty()){
                log.error(""+settings + " "+rarity+ " "+ stat);
            }
            try{
                int inserted = dsl.insertInto(TRADE_UP_SKINS, TRADE_UP_SKINS.CUSTOM, TRADE_UP_SKINS.TRADE_UP_ID, TRADE_UP_SKINS.C_S2_SKIN_ID).valuesOfRows(rowList).execute();
                if(inserted != tupSkinIds.size()){
                    throw new RuntimeException("Error inserting TradeUpSkins");
                }
            }catch (Exception e){
                throw new RuntimeException("Exception inserting TradeUpSkins\n"+e.getMessage());
            }
        }
    }

}
