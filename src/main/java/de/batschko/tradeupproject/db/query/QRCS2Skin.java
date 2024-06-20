package de.batschko.tradeupproject.db.query;


import de.batschko.tradeupproject.enums.Condition;
import de.batschko.tradeupproject.tables.CS2Skin;
import de.batschko.tradeupproject.tables.SkinPrice;
import de.batschko.tradeupproject.tables.TradeUpSkins;
import de.batschko.tradeupproject.utils.SkinUtils;
import lombok.extern.slf4j.Slf4j;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Record6;
import org.jooq.Result;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

import static de.batschko.tradeupproject.tables.CS2Skin.C_S2_SKIN;
import static de.batschko.tradeupproject.tables.TradeUpOutcomeCustom.TRADE_UP_OUTCOME_CUSTOM;
import static de.batschko.tradeupproject.tables.TradeUpOutcomeSkinsCustom.TRADE_UP_OUTCOME_SKINS_CUSTOM;
import static de.batschko.tradeupproject.tables.TradeUpSkins.TRADE_UP_SKINS;
import static de.batschko.tradeupproject.tables.TradeUpSkinsCustom.TRADE_UP_SKINS_CUSTOM;
import static de.batschko.tradeupproject.tables.VFullcs2skin.V_FULLCS2SKIN;
import static de.batschko.tradeupproject.tables.VOutSkinsCsmoney.V_OUT_SKINS_CSMONEY;
import static de.batschko.tradeupproject.tables.VTradeupskinsCsmoney.V_TRADEUPSKINS_CSMONEY;
import static org.jooq.impl.DSL.avg;

/**
 * Database access related to {@link CS2Skin}.
 */
@Slf4j
@Repository
public class QRCS2Skin extends QueryRepository {


    public QRCS2Skin(DSLContext dslContext) {
        super(dslContext);
    }


    /**
     * Get all {@link CS2Skin}s.
     *
     * @return list of {@link CS2Skin}
     */
    public static List<CS2Skin> getAll() {
        return dsl.select()
                .from(C_S2_SKIN)
                .fetchInto(CS2Skin.class);
    }

    /**
     * Get all {@link CS2Skin}s with limit.
     *
     * @param limit limit
     * @return list of {@link CS2Skin}
     */
    public static List<CS2Skin> getLimit(int limit) {
        return dsl.select()
                .from(C_S2_SKIN)
                .limit(limit)
                .fetchInto(CS2Skin.class);
    }

    /**
     * TODO
     */
    public static Record getFullSkin(int id) {
        return dsl.select()
                .from(V_FULLCS2SKIN)
                .where(V_FULLCS2SKIN.ID.eq(id))
                .fetchOne();
    }


    /**
     * Gets {@link CS2Skin} id by stashHolderId, condition and stattrak.
     *
     * @param stashHolderId stash holder id
     * @param condition     condition {@link Condition}
     * @param stattrak      stattrak as byte
     * @return id for {@link CS2Skin}
     */
    public static int getByStashHolderConditionStattrak(int stashHolderId, Condition condition, byte stattrak) {
        Integer result = dsl.select(C_S2_SKIN.ID)
                .from(C_S2_SKIN)
                .where(C_S2_SKIN.STASH_ID.eq(stashHolderId))
                .and(C_S2_SKIN.CONDITION.eq(condition))
                .and(C_S2_SKIN.STATTRAK.eq(stattrak))
                .fetchOneInto(Integer.class);
        if (result == null)
            throw new RuntimeException("Couldn't get StashHolder Condition and stattrak: StashHolderId -> " + stashHolderId + " condition -> " + condition + " stattrak -> " + stattrak);
        return result;

    }


    /**
     * Gets average price for {@link TradeUpSkins} by collection name, condition and TradeUp id.
     *
     * @param collName  collection name
     * @param condition condition {@link Condition}
     * @param tupId     tradeUp id
     * @return avg price or -3 if no records are found
     */
    public static double getTradeUpSkinsAveragePrice(String collName, Condition condition, int tupId) {

            Double result = dsl.select(avg(V_FULLCS2SKIN.PRICE))
                    .from(V_FULLCS2SKIN)
                    .join(TRADE_UP_SKINS)
                    .on(TRADE_UP_SKINS.C_S2_SKIN_ID.eq(V_FULLCS2SKIN.ID))
                    .where(V_FULLCS2SKIN.COLL_NAME.eq(collName))
                    .and(V_FULLCS2SKIN.CONDITION.eq(condition))
                    .and(TRADE_UP_SKINS.TRADE_UP_ID.eq(tupId))
                    .fetchOneInto(Double.class);
            if (result==null){
                log.debug("Couldn't get average TradeUpSkins price for: tupId -> " + tupId + " coll -> " + collName + " cond -> " + condition);
                return -3;
            }
            return result;
    }

    /**
     * Gets average amount sold for {@link TradeUpSkins} by collection name, condition and TradeUp id.
     *
     * @param collName  collection name
     * @param condition condition {@link Condition}
     * @param tupId     tradeUp id
     * @return avg amount sold or -3 if no records are found
     */
    public static double getTradeUpSkinAverageAmountSold(String collName, Condition condition, int tupId) {
        Double result = dsl.select(avg(V_FULLCS2SKIN.AMOUNT_SOLD))
                .from(V_FULLCS2SKIN)
                .join(TRADE_UP_SKINS)
                .on(TRADE_UP_SKINS.C_S2_SKIN_ID.eq(V_FULLCS2SKIN.ID))
                .where(V_FULLCS2SKIN.COLL_NAME.eq(collName))
                .and(V_FULLCS2SKIN.CONDITION.eq(condition))
                .and(TRADE_UP_SKINS.TRADE_UP_ID.eq(tupId))
                .fetchOneInto(Double.class);
        if (result==null){
            log.debug("Couldn't get average TradeUpSkins amount sold for: tupId -> " + tupId + " coll -> " + collName + " cond -> " + condition);
            return -3;
        }
        return result;

    }


    /**
     * Gets {@link CS2Skin}s without price as {@link SkinUtils.SkinFullName}.
     *
     * @param limit       limit
     * @param specialChar true -> use special chars
     * @return list of {@link SkinUtils.SkinFullName}
     */
    public static List<SkinUtils.SkinFullName> getSkinsWithoutPrice(int limit, boolean specialChar) {
        List<String> specialChars = new ArrayList<>(SkinUtils.getSpecialSkinNamesMap().values());
        org.jooq.Condition condSpecialChars;
        if(specialChar){
            condSpecialChars = V_FULLCS2SKIN.TITLE.in(specialChars);
        }else {
            condSpecialChars = V_FULLCS2SKIN.TITLE.notIn(specialChars);
        }

        Result<Record6<Integer, Byte, String, String, Condition, Double>> result = dsl.select(
                        V_FULLCS2SKIN.ID, V_FULLCS2SKIN.STATTRAK, V_FULLCS2SKIN.WEAPON,
                        V_FULLCS2SKIN.TITLE, V_FULLCS2SKIN.CONDITION, V_FULLCS2SKIN.PRICE)
                .from(V_FULLCS2SKIN)
                .where(condSpecialChars.and(V_FULLCS2SKIN.SKIN_PRICE_ID.isNull()))
                .limit(limit)
                .fetch();
        List<SkinUtils.SkinFullName> resultList = new ArrayList<>();
        result.forEach(record -> {
            SkinUtils.SkinFullName test = new SkinUtils.SkinFullName(
                    record.get(0, Integer.class),
                    record.get(1, Byte.class),
                    record.get(2, String.class),
                    record.get(3, String.class),
                    record.get(4, Condition.class));
            resultList.add(test);
        });
        return resultList;
    }


    /**
     * Update {@link CS2Skin} price with {@link SkinPrice}.
     *
     * @param skinId      {@link CS2Skin} id
     * @param skinPriceId {@link SkinPrice} id
     */
    public static void updatePrice(int skinId, int skinPriceId) {
        dsl.update(C_S2_SKIN).set(C_S2_SKIN.SKIN_PRICE_ID, skinPriceId).where(C_S2_SKIN.ID.eq(skinId)).execute();

    }


    /**
     * Remove all price ids from all {@link CS2Skin}s.
     * <p>->dangerous!<-</p>
     */
    public static void removeAllPriceIds() {
        dsl.update(C_S2_SKIN).set(C_S2_SKIN.SKIN_PRICE_ID, (Integer) null).execute();
    }


    public static Result<Record> getTradeUpSkins(int id){

        return dsl.select()
                .from(V_TRADEUPSKINS_CSMONEY)
                .where(V_TRADEUPSKINS_CSMONEY.TRADE_UP_ID.eq(id))
                .fetch();
    }
    public static Result<Record> getTradeUpSkinsCustom(int id){

        return dsl.select()
                .from(V_FULLCS2SKIN)
                .join(TRADE_UP_SKINS_CUSTOM)
                .on(TRADE_UP_SKINS_CUSTOM.C_S2_SKIN_ID.eq(V_FULLCS2SKIN.ID))
                .where(TRADE_UP_SKINS_CUSTOM.TRADE_UP_CUSTOM_ID.eq(id))
                .fetch();
    }

    public static Result<Record> getOutSkinsCustom(int id){

        return dsl.select()
                .from(V_FULLCS2SKIN)
                .join(TRADE_UP_OUTCOME_SKINS_CUSTOM)
                .on(TRADE_UP_OUTCOME_SKINS_CUSTOM.C_S2_SKIN_ID.eq(V_FULLCS2SKIN.ID))
                .where(TRADE_UP_OUTCOME_SKINS_CUSTOM.TRADE_UP_CUSTOM_ID.eq(id))
                .fetch();
    }

    //TODO remove
    public static void deleteInAndOutSkins(int id){
        dsl.deleteFrom(TRADE_UP_SKINS_CUSTOM).where(TRADE_UP_SKINS_CUSTOM.TRADE_UP_CUSTOM_ID.eq(id)).execute();
        dsl.deleteFrom(TRADE_UP_OUTCOME_SKINS_CUSTOM).where(TRADE_UP_OUTCOME_SKINS_CUSTOM.TRADE_UP_CUSTOM_ID.eq(id)).execute();
    }


    public static Result<Record> getOutSkins(int id){

        return dsl.select()
                .from(V_OUT_SKINS_CSMONEY)
                .where(V_OUT_SKINS_CSMONEY.TRADE_UP_ID.eq(id))
                .fetch();
    }

    public static int getSkinIdByName(String weapon, String title, Condition condition, int tupId){
        int id =  dsl.select(V_OUT_SKINS_CSMONEY.ID)
                .from(V_OUT_SKINS_CSMONEY)
                .where(V_OUT_SKINS_CSMONEY.TRADE_UP_ID.eq(tupId))
                .and(V_OUT_SKINS_CSMONEY.WEAPON.eq(weapon))
                .and(V_OUT_SKINS_CSMONEY.TITLE.eq(title))
                .and(V_OUT_SKINS_CSMONEY.CONDITION.eq(condition))
                .fetchOneInto(Integer.class);
        return id;
}
}