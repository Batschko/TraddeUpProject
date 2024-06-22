package de.batschko.tradeupproject.db.query.api;

import de.batschko.tradeupproject.db.query.QueryRepository;
import de.batschko.tradeupproject.enums.Condition;
import de.batschko.tradeupproject.enums.Rarity;
import de.batschko.tradeupproject.enums.TradeUpStatus;
import de.batschko.tradeupproject.tables.TradeUp;
import de.batschko.tradeupproject.tables.TradeUpMade;
import de.batschko.tradeupproject.tables.TradeUpMarked;
import de.batschko.tradeupproject.tables.records.TradeUpMadeRecord;
import de.batschko.tradeupproject.tables.records.TradeUpMarkedRecord;
import lombok.extern.slf4j.Slf4j;
import org.jooq.Record;
import org.jooq.*;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static de.batschko.tradeupproject.tables.TradeUpMade.TRADE_UP_MADE;
import static de.batschko.tradeupproject.tables.TradeUpMarked.TRADE_UP_MARKED;
import static de.batschko.tradeupproject.tables.VFullTradeup.V_FULL_TRADEUP;
import static de.batschko.tradeupproject.tables.VFullcs2skin.V_FULLCS2SKIN;
import static org.jooq.impl.DSL.count;
import static org.jooq.impl.DSL.sum;

/**
 * Database access related to api access for {@link TradeUp}, {@link TradeUpMarked}, {@link TradeUpMade}.
 * <p>{@link TradeUp}s are joined with {@link TradeUpMarked}</p>
 */
@Slf4j
@Repository
public class QRTradeUpTable extends QueryRepository {
    public QRTradeUpTable(DSLContext dslContext) {
        super(dslContext);
    }


    /**
     * Get TableTradeUps.
     *
     * @return {@code Result<Record>} all {@link TradeUp} & {@link TradeUpMarked} fields
     */
    public static Result<Record> getTradeUps(boolean custom){
        return dsl.select(V_FULL_TRADEUP.fields()).select(TRADE_UP_MARKED.MARKED, TRADE_UP_MARKED.WATCH, TRADE_UP_MARKED.ACTIVE)
                .from(V_FULL_TRADEUP)
                .leftJoin(TRADE_UP_MARKED)
                .on(V_FULL_TRADEUP.ID.eq(TRADE_UP_MARKED.TRADE_UP_ID))
                .where(V_FULL_TRADEUP.STATUS.eq(TradeUpStatus.CALCULATED))
                .and(V_FULL_TRADEUP.OUTCOME.gt(0.0))
                .and(V_FULL_TRADEUP.CUSTOM.eq((byte) (custom?1:0)))
                .fetch();
    }

    /**
     * Get {@link TradeUp} ids for TradeUps marked as active.
     *
     * @param custom custom
     * @return list of tradeUp ids
     */
    public static List<Integer> getTradeUpsActiveIds(boolean custom){
        return  dsl.select(TRADE_UP_MARKED.TRADE_UP_ID)
                .from(TRADE_UP_MARKED)
                .where(TRADE_UP_MARKED.ACTIVE.eq((byte)1))
                .and(TRADE_UP_MARKED.CUSTOM.eq((byte) (custom?1:0)))
                .fetchInto(Integer.class);
    }

    /**
     * Get TableTradeUps that are marked.
     *
     * @param custom custom
     * @return {@code Result<Record>} all {@link TradeUp} & {@link TradeUpMarked} fields
     */
    public static Result<Record> getTradeUpsMarked(boolean custom){
        return dsl.select(V_FULL_TRADEUP.fields()).select(TRADE_UP_MARKED.MARKED, TRADE_UP_MARKED.WATCH, TRADE_UP_MARKED.ACTIVE)
                .from(V_FULL_TRADEUP)
                .join(TRADE_UP_MARKED)
                .on(V_FULL_TRADEUP.ID.eq(TRADE_UP_MARKED.TRADE_UP_ID)).and(V_FULL_TRADEUP.CUSTOM.eq(TRADE_UP_MARKED.CUSTOM))
                .where(V_FULL_TRADEUP.CUSTOM.eq((byte) (custom?1:0)))
                .fetch();
    }

    /**
     * Get TableTradeUps that are watched.
     *
     * @param custom custom
     * @return {@code Result<Record>} all {@link TradeUp} & {@link TradeUpMarked} fields
     */
    public static Result<Record> getTradeUpsWatched(boolean custom){
        return dsl.select(V_FULL_TRADEUP.fields()).select(TRADE_UP_MARKED.MARKED, TRADE_UP_MARKED.WATCH, TRADE_UP_MARKED.ACTIVE)
                .from(V_FULL_TRADEUP)
                .join(TRADE_UP_MARKED)
                .on(V_FULL_TRADEUP.ID.eq(TRADE_UP_MARKED.TRADE_UP_ID)).and(V_FULL_TRADEUP.CUSTOM.eq(TRADE_UP_MARKED.CUSTOM))
                .and(TRADE_UP_MARKED.WATCH.eq((byte)1))
                .and(TRADE_UP_MARKED.CUSTOM.eq((byte) (custom?1:0)))
                .fetch();
    }

    /**
     * Get TableTradeUps that are active.
     *
     * @param custom custom
     * @return {@code Result<Record>} all {@link TradeUp} & {@link TradeUpMarked} fields
     */
    public static Result<Record> getTradeUpsActive(boolean custom){
        return dsl.select(V_FULL_TRADEUP.fields()).select(TRADE_UP_MARKED.MARKED, TRADE_UP_MARKED.WATCH, TRADE_UP_MARKED.ACTIVE)
                .from(V_FULL_TRADEUP)
                .join(TRADE_UP_MARKED)
                .on(V_FULL_TRADEUP.ID.eq(TRADE_UP_MARKED.TRADE_UP_ID)).and(V_FULL_TRADEUP.CUSTOM.eq(TRADE_UP_MARKED.CUSTOM))
                .where(TRADE_UP_MARKED.ACTIVE.eq((byte)1))
                .and(TRADE_UP_MARKED.CUSTOM.eq((byte) (custom?1:0)))
                .fetch();
    }

    /**
     * Get {@link TradeUpMade}s as {@link JSONObject}.
     * <p>JSONObject -> "tups": jsonArray(TradeUps), "profit": profit</p>
     * @return {@link TradeUpMade}s as {@link JSONObject} -> "tups": jsonArray(TradeUps), "profit": profit
     */
    public static JSONObject getTradeUpsMade(){
        Result<Record> a =  dsl.select(TRADE_UP_MADE.fields()).select(V_FULLCS2SKIN.IMAGE_URL)
                .from(TRADE_UP_MADE)
                .leftJoin(V_FULLCS2SKIN).on(V_FULLCS2SKIN.ID.eq(TRADE_UP_MADE.C_S2_SKIN_ID))
                .fetch();
        List<Map<String, Object>> maps = a.intoMaps();
        // Convert list of maps to JSON array
        JSONArray jsonArray = new JSONArray();
        double profit=0;
        for (Map<String, Object> map : maps) {
            JSONObject jsonObject = new JSONObject();
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                jsonObject.put(entry.getKey(), entry.getValue());
            }
            jsonObject.put("condition", jsonObject.getEnum(Condition.class,"condition"));
            if(jsonObject.has("rarity"))jsonObject.put("rarity", jsonObject.getEnum(Rarity.class,"rarity"));
            jsonObject.put("modified_date", jsonObject.get("modified_date").toString());

            jsonArray.put(jsonObject);
            profit += jsonObject.getDouble("profit");
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("tups", jsonArray);
        jsonObject.put("profit", profit);

        return jsonObject;
    }

    /**
     * Get grouped {@link TradeUpMade}s data.
     * <p>tupmade id, tup id, stat, rarity, gsettings, sum profit, made count, avg profit</p>
     * @return {@link JSONArray} of {@link JSONObject}s -> madeTup id, tup id, stat, rarity, gsettings, sum profit, made count, avg profit
     */
    public static JSONArray getTradeUpsMadeGrouped(){
        Result<Record8<Integer, Integer, Byte, Rarity, String, BigDecimal, Integer, BigDecimal>> a =  dsl.select(TRADE_UP_MADE.ID, TRADE_UP_MADE.TRADE_UP_ID, TRADE_UP_MADE.STATTRAK,
                        TRADE_UP_MADE.RARITY, TRADE_UP_MADE.GENERATION_SETTINGS,
                        sum(TRADE_UP_MADE.PROFIT), count(TRADE_UP_MADE.TRADE_UP_ID), sum(TRADE_UP_MADE.PROFIT).div(count(TRADE_UP_MADE.TRADE_UP_ID)))
                .from(TRADE_UP_MADE)
                .groupBy(TRADE_UP_MADE.TRADE_UP_ID)
                .fetch();
        List<Map<String, Object>> maps = a.intoMaps();
        // Convert list of maps to JSON array
        JSONArray jsonArray = new JSONArray();
        for (Map<String, Object> map : maps) {
            JSONObject jsonObject = new JSONObject();
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                jsonObject.put(entry.getKey(), entry.getValue());
            }
            if(jsonObject.has("rarity"))jsonObject.put("rarity", jsonObject.getEnum(Rarity.class,"rarity"));
            jsonArray.put(jsonObject);
        }
        return jsonArray;
    }

    /**
     * Toggle mark for a {@link TradeUp}.
     *
     * @param id     tradeUp id
     * @param custom custom
     */
    public static void toggleMarkTradeUp(int id, boolean custom){
        //check if tup is already marked
        Record exists =  dsl.select()
                .from(TRADE_UP_MARKED)
                .where(TRADE_UP_MARKED.TRADE_UP_ID.eq(id))
                .and(TRADE_UP_MARKED.CUSTOM.eq((byte) (custom?1:0)))
                .fetchOne();

        if(exists!=null){
            dsl.delete(TRADE_UP_MARKED).where(TRADE_UP_MARKED.TRADE_UP_ID.eq(id)).and(TRADE_UP_MARKED.CUSTOM.eq((byte) (custom?1:0))).execute();
            return;
        }
        Record5<Integer, Byte, Rarity, Integer, String> result =  dsl.select(V_FULL_TRADEUP.ID, V_FULL_TRADEUP.STATTRAK, V_FULL_TRADEUP.RARITY, V_FULL_TRADEUP.FLOAT_DICT_ID, V_FULL_TRADEUP.SETTINGS)
                .from(V_FULL_TRADEUP)
                .where(V_FULL_TRADEUP.ID.eq(id))
                .and(V_FULL_TRADEUP.CUSTOM.eq((byte)0))
                .fetchOne();
        TradeUpMarkedRecord markedRecord = dsl.newRecord(TRADE_UP_MARKED);
        markedRecord.setTradeUpId(result.get(0, Integer.class));
        markedRecord.setCustom((byte) 0);
        markedRecord.setStattrak(result.get(1, Byte.class));
        markedRecord.setRarity(result.get(2, Rarity.class));
        markedRecord.setFloatDictId(result.get(3, Integer.class));
        markedRecord.setGenerationSettings(result.get(4, String.class));
        markedRecord.store();
    }

    /**
     * Toggle {@link TradeUpMarked} watched.
     *
     * @param id     tradeUp id
     * @param custom custom
     */
    public static void toggleWatch(int id, boolean custom){
        //check if tup is already marked
        Record exists =  dsl.select()
                .from(TRADE_UP_MARKED)
                .where(TRADE_UP_MARKED.TRADE_UP_ID.eq(id))
                .and(TRADE_UP_MARKED.WATCH.eq((byte)1))
                .and(TRADE_UP_MARKED.CUSTOM.eq((byte) (custom?1:0)))
                .fetchOne();

        if(exists!=null){
            dsl.update(TRADE_UP_MARKED).set(TRADE_UP_MARKED.WATCH, (byte)0).where(TRADE_UP_MARKED.TRADE_UP_ID.eq(id)).and(TRADE_UP_MARKED.CUSTOM.eq((byte) (custom?1:0))).execute();
        }else {
            dsl.update(TRADE_UP_MARKED).set(TRADE_UP_MARKED.WATCH, (byte)1).where(TRADE_UP_MARKED.TRADE_UP_ID.eq(id)).and(TRADE_UP_MARKED.CUSTOM.eq((byte) (custom?1:0))).execute();
        }
    }

    /**
     * Toggle {@link TradeUpMarked} active.
     *
     * @param id     tradeUp id
     * @param custom custom
     */
    public static void toggleActive(int id, boolean custom){
        //check if tup is already active
        Record exists =  dsl.select()
                .from(TRADE_UP_MARKED)
                .where(TRADE_UP_MARKED.TRADE_UP_ID.eq(id))
                .and(TRADE_UP_MARKED.ACTIVE.eq((byte)1))
                .and(TRADE_UP_MARKED.CUSTOM.eq((byte) (custom?1:0)))
                .fetchOne();

        if(exists!=null){
            dsl.update(TRADE_UP_MARKED).set(TRADE_UP_MARKED.ACTIVE, (byte)0).where(TRADE_UP_MARKED.TRADE_UP_ID.eq(id)).and(TRADE_UP_MARKED.CUSTOM.eq((byte) (custom?1:0))).execute();
        }else {
            dsl.update(TRADE_UP_MARKED).set(TRADE_UP_MARKED.ACTIVE, (byte)1).where(TRADE_UP_MARKED.TRADE_UP_ID.eq(id)).and(TRADE_UP_MARKED.CUSTOM.eq((byte) (custom?1:0))).execute();
        }
    }


    /**
     * Save {@link TradeUpMade} to database.
     *
     * @param custom      custom
     * @param tradeUpId   tradeUp id
     * @param stattrak    stattrak
     * @param rarity      rarity
     * @param floatDictId float dict id
     * @param gsettings   gsettings
     * @param cs2skinId   skin id
     * @param skinName    skin name
     * @param cond        cond
     * @param cost        cost
     * @param price       price
     * @return 1 if saved successfully, else 0
     */
    public static int saveMadeTradeUp(byte custom, int tradeUpId, byte stattrak, Rarity rarity, int floatDictId, String gsettings, int cs2skinId, String skinName, Condition cond, double cost, double price){
        TradeUpMadeRecord record = dsl.newRecord(TRADE_UP_MADE);
        record.setTradeUpId(tradeUpId);
        record.setCustom(custom);
        record.setStattrak(stattrak);
        record.setRarity(rarity);
        record.setFloatDictId(floatDictId);
        record.setGenerationSettings(gsettings);
        record.setCS2SkinId(cs2skinId);
        record.setSkinName(skinName);
        record.setCondition(cond);
        record.setCost(cost);
        record.setPrice(price);
        return record.store();
    }

}

