package de.batschko.tradeupproject.db.query;


import de.batschko.tradeupproject.enums.Condition;
import de.batschko.tradeupproject.tables.CS2Skin;
import de.batschko.tradeupproject.tables.TradeUpSkins;
import lombok.extern.slf4j.Slf4j;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Result;
import org.springframework.stereotype.Repository;

import java.util.List;

import static de.batschko.tradeupproject.tables.CS2Skin.C_S2_SKIN;
import static de.batschko.tradeupproject.tables.TradeUpSkins.TRADE_UP_SKINS;
import static de.batschko.tradeupproject.tables.VFullcs2skin.V_FULLCS2SKIN;
import static de.batschko.tradeupproject.tables.VOutSkins.V_OUT_SKINS;
import static de.batschko.tradeupproject.tables.VTradeupskins.V_TRADEUPSKINS;
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
     * Remove all skin prices from all {@link CS2Skin}s.
     * <p>->dangerous!<-</p>
     */
    public static void removeAllPrices() {
        dsl.update(C_S2_SKIN).set(C_S2_SKIN.PRICE, (Double) null).execute();
    }


    public static Result<Record> getTradeUpSkins(int id){

        return dsl.select()
                .from(V_TRADEUPSKINS)
                .where(V_TRADEUPSKINS.TRADE_UP_ID.eq(id))
                .fetch();
    }

    /*
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
*/

    public static Result<Record> getOutSkins(int id){

        return dsl.select()
                .from(V_OUT_SKINS)
                .where(V_OUT_SKINS.TRADE_UP_ID.eq(id))
                .fetch();
    }

    public static int getOutSkinIdByNameCondTup(String weapon, String title, Condition condition, int tupId){
        int id =  dsl.select(V_OUT_SKINS.ID)
                .from(V_OUT_SKINS)
                .where(V_OUT_SKINS.TRADE_UP_ID.eq(tupId))
                .and(V_OUT_SKINS.WEAPON.eq(weapon))
                .and(V_OUT_SKINS.TITLE.eq(title))
                .and(V_OUT_SKINS.CONDITION.eq(condition))
                .fetchOneInto(Integer.class);
        return id;
}
}