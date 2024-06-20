package de.batschko.tradeupproject.db.query;

import de.batschko.tradeupproject.enums.Condition;
import de.batschko.tradeupproject.enums.Rarity;
import de.batschko.tradeupproject.tables.TradeUpOutcomeSkins;
import de.batschko.tradeupproject.tables.TradeUpSkins;
import de.batschko.tradeupproject.tables.records.TradeUpOutcomeCustomRecord;
import de.batschko.tradeupproject.tables.records.TradeUpOutcomeRecord;
import de.batschko.tradeupproject.tables.records.TradeUpOutcomeSkinsCustomRecord;
import de.batschko.tradeupproject.tables.records.TradeUpOutcomeSkinsRecord;
import de.batschko.tradeupproject.tradeup.TradeUpSettings;
import de.batschko.tradeupproject.utils.SkinUtils;
import org.jetbrains.annotations.NotNull;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Record5;
import org.jooq.Result;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static de.batschko.tradeupproject.tables.CS2Skin.C_S2_SKIN;
import static de.batschko.tradeupproject.tables.FloatDictionary.FLOAT_DICTIONARY;
import static de.batschko.tradeupproject.tables.StashSkinHolder.STASH_SKIN_HOLDER;
import static de.batschko.tradeupproject.tables.TradeUpOutcome.TRADE_UP_OUTCOME;
import static de.batschko.tradeupproject.tables.TradeUpOutcomeCustom.TRADE_UP_OUTCOME_CUSTOM;
import static de.batschko.tradeupproject.tables.TradeUpOutcomeSkins.TRADE_UP_OUTCOME_SKINS;
import static de.batschko.tradeupproject.tables.TradeUpOutcomeSkinsCustom.TRADE_UP_OUTCOME_SKINS_CUSTOM;
import static de.batschko.tradeupproject.tables.TradeUpSkins.TRADE_UP_SKINS;
import static de.batschko.tradeupproject.tables.VTupnsettinggs.V_TUPNSETTINGGS;

/**
 * Database access for utility methods.
 */
@Repository
public class QRUtils extends QueryRepository {

    public QRUtils(DSLContext dslContext) {
        super(dslContext);
    }


    /**
     * Gets float dictionary map by id.
     *
     * @param floatDictId float dict id
     * @return map of {@link Condition}, {@link Double}
     */
    public static Map<Condition, Double> getFloatDictMap(int floatDictId) {
        Record result = dsl.select()
                .from(FLOAT_DICTIONARY)
                .where(FLOAT_DICTIONARY.ID.eq(floatDictId))
                .fetchOne();
        if (result == null) {
            throw new RuntimeException("Couldn't query FloatDictionary with id: " + floatDictId);
        }
        Map<Condition, Double> floatMap = new HashMap<>();
        for (Condition cond : Condition.values()) {
            floatMap.put(cond, result.getValue(cond.toString(), Double.class));
        }
        return floatMap;
    }

    /**
     * Gets {@link TradeUpSkins} as {@link SkinUtils.SkinFullName}.
     *
     * @param tupId tradeup id
     * @return list of {@link SkinUtils.SkinFullName}
     */
    public static List<SkinUtils.SkinFullName> getTradeUpSkins(int tupId) {
        Result<Record5<Integer, Byte, String, String, Condition>> result = dsl.select(
                        C_S2_SKIN.ID, C_S2_SKIN.STATTRAK, STASH_SKIN_HOLDER.WEAPON,
                        STASH_SKIN_HOLDER.TITLE, C_S2_SKIN.CONDITION)
                .from(TRADE_UP_SKINS)
                .join(C_S2_SKIN)
                .on(C_S2_SKIN.ID.eq(TRADE_UP_SKINS.C_S2_SKIN_ID))
                .join(STASH_SKIN_HOLDER)
                .on(STASH_SKIN_HOLDER.STASH_ID.eq(C_S2_SKIN.STASH_ID))
                .where(TRADE_UP_SKINS.TRADE_UP_ID.eq(tupId))
                .fetch();
        List<SkinUtils.SkinFullName> resultList = new ArrayList<>();
        result.forEach(record -> {
            SkinUtils.SkinFullName test = new SkinUtils.SkinFullName(
                    record.get(C_S2_SKIN.ID, Integer.class),
                    record.get(C_S2_SKIN.STATTRAK, Byte.class),
                    record.get(STASH_SKIN_HOLDER.WEAPON, String.class),
                    record.get(STASH_SKIN_HOLDER.TITLE, String.class),
                    record.get(C_S2_SKIN.CONDITION, Condition.class));
            resultList.add(test);
        });
        return resultList;
    }

    /**
     * Gets {@link SkinUtils.TradeUpSkinInfo}s for tradeup.
     * @param tupId tradeup id
     * @return list of {@link SkinUtils.TradeUpSkinInfo}s
     */
    public static List<SkinUtils.TradeUpSkinInfo> getTradeUpSkinsInfo(int tupId) {
        Result<Record> tupAndSettings = dsl.select()
                .from(V_TUPNSETTINGGS)
                .where(V_TUPNSETTINGGS.ID.eq(tupId))
                .fetch();
        List<SkinUtils.TradeUpSkinInfo> resultList = new ArrayList<>();
        for (Record tupAndS : tupAndSettings) {
            byte stat = tupAndS.get("stattrak", Byte.class);
            Rarity rarity = tupAndS.get("rarity", Rarity.class);
            TradeUpSettings settings = TradeUpSettings.deserialize(tupAndS.get("settings", String.class));
            List<SkinUtils.TradeUpSkinInfo> infos = settings.getTradeUpSkinInfo(rarity, stat);
            resultList.addAll(infos);
        }
        return resultList;
    }

    /**
     * Gets {@link TradeUpOutcomeSkins} as {@link SkinUtils.SkinFullName}.
     *
     * @param tupId tradeup id
     * @return list of {@link SkinUtils.SkinFullName}
     */
    public static List<SkinUtils.SkinFullName> getTradeUpOutcomeSkins(int tupId) {
        Result<Record5<Integer, Byte, String, String, Condition>> result = dsl.select(
                        C_S2_SKIN.ID, C_S2_SKIN.STATTRAK, STASH_SKIN_HOLDER.WEAPON,
                        STASH_SKIN_HOLDER.TITLE, C_S2_SKIN.CONDITION)
                .from(TRADE_UP_OUTCOME_SKINS)
                .join(C_S2_SKIN)
                .on(C_S2_SKIN.ID.eq(TRADE_UP_OUTCOME_SKINS.C_S2_SKIN_ID))
                .join(STASH_SKIN_HOLDER)
                .on(STASH_SKIN_HOLDER.STASH_ID.eq(C_S2_SKIN.STASH_ID))
                .where(TRADE_UP_OUTCOME_SKINS.TRADE_UP_ID.eq(tupId))
                .fetch();
        List<SkinUtils.SkinFullName> resultList = new ArrayList<>();
        result.forEach(record -> {
            SkinUtils.SkinFullName test = new SkinUtils.SkinFullName(
                    record.get(C_S2_SKIN.ID, Integer.class),
                    record.get(C_S2_SKIN.STATTRAK, Byte.class),
                    record.get(STASH_SKIN_HOLDER.WEAPON, String.class),
                    record.get(STASH_SKIN_HOLDER.TITLE, String.class),
                    record.get(C_S2_SKIN.CONDITION, Condition.class));
            resultList.add(test);
        });
        return resultList;
    }

    /**
     * Creates empty {@link TradeUpOutcomeRecord}.
     *
     * @return {@link TradeUpOutcomeRecord}
     */
    public static TradeUpOutcomeRecord createRecordTradeUpOutcome(){
        return dsl.newRecord(TRADE_UP_OUTCOME);
    }
    /**
     * Creates empty {@link TradeUpOutcomeCustomRecord}.
     *
     * @return {@link TradeUpOutcomeCustomRecord}
     */
    public static TradeUpOutcomeCustomRecord createRecordTradeUpOutcomeCustome(){
        return dsl.newRecord(TRADE_UP_OUTCOME_CUSTOM);
    }
    /**
     * Creates empty {@link TradeUpOutcomeSkinsRecord}.
     *
     * @return  {@link TradeUpOutcomeSkinsRecord}
     */
    public static TradeUpOutcomeSkinsRecord createRecordTradeUpOutcomeSkins(){
        return dsl.newRecord(TRADE_UP_OUTCOME_SKINS);
    }
    public static TradeUpOutcomeSkinsCustomRecord createRecordTradeUpOutcomeSkinsCustom(){
        return dsl.newRecord(TRADE_UP_OUTCOME_SKINS_CUSTOM);
    }
}

