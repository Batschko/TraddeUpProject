package de.batschko.tradeupproject.db.query;


import de.batschko.tradeupproject.enums.Condition;
import de.batschko.tradeupproject.enums.Rarity;
import de.batschko.tradeupproject.tables.*;
import de.batschko.tradeupproject.tables.records.VOutSkinsRecord;
import de.batschko.tradeupproject.tables.records.VTradeupskinsRecord;
import lombok.extern.slf4j.Slf4j;
import org.jooq.Record;
import org.jooq.*;
import org.springframework.stereotype.Repository;

import java.util.List;

import static de.batschko.tradeupproject.Tables.TRADE_UP_OUTCOME_SKINS;
import static de.batschko.tradeupproject.tables.CS2Skin.C_S2_SKIN;
import static de.batschko.tradeupproject.tables.StashSkinHolder.STASH_SKIN_HOLDER;
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
     * Gets {@link CS2Skin} as {@link VFullcs2skin}.
     *
     * @param id skin id
     * @return Record {@link VFullcs2skin}
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
    public static double getTradeUpSkinsAveragePrice(boolean custom, String collName, Condition condition, int tupId) {

            Double result = dsl.select(avg(V_FULLCS2SKIN.PRICE))
                    .from(V_FULLCS2SKIN)
                    .join(TRADE_UP_SKINS)
                    .on(TRADE_UP_SKINS.C_S2_SKIN_ID.eq(V_FULLCS2SKIN.ID).and(TRADE_UP_SKINS.CUSTOM.eq((byte) (custom?1:0))))
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


    /**
     * Gets {@link TradeUpSkins} as {@link VTradeupskins}.
     *
     * @param id tradeUp id
     * @return {@code Result<Record>} {@link VTradeupskinsRecord}
     */
    public static Result<Record> getTradeUpSkins(int id, byte custom){

        return dsl.select()
                .from(V_TRADEUPSKINS)
                .where(V_TRADEUPSKINS.TRADE_UP_ID.eq(id))
                .and(V_TRADEUPSKINS.CUSTOM.eq(custom))
                .fetch();
    }

    /**
     * Gets custom {@link TradeUpSkins} as {@link VTradeupskins}.
     *
     * @param id custom tradeUp id
     * @return {@code Result<Record>} {@link VTradeupskinsRecord}
     */
    public static Result<Record> getTradeUpSkinsCustom(int id){
        return dsl.select()
                .from(V_FULLCS2SKIN)
                .join(TRADE_UP_SKINS)
                .on(TRADE_UP_SKINS.C_S2_SKIN_ID.eq(V_FULLCS2SKIN.ID))
                .where(TRADE_UP_SKINS.TRADE_UP_ID.eq(id))
                .and(TRADE_UP_SKINS.CUSTOM.eq((byte) 1))
                .fetch();
    }

    /**
     * Gets custom {@link TradeUpOutcomeSkins} as {@link VOutSkins}.
     *
     * @param id custom tradeUp id
     * @return Result < Record > {@link VOutSkinsRecord}
     */
    public static Result<Record> getOutSkinsCustom(int id){
        return dsl.select()
                .from(V_FULLCS2SKIN)
                .join(TRADE_UP_OUTCOME_SKINS)
                .on(TRADE_UP_OUTCOME_SKINS.C_S2_SKIN_ID.eq(V_FULLCS2SKIN.ID))
                .where(TRADE_UP_OUTCOME_SKINS.TRADE_UP_ID.eq(id))
                .and(TRADE_UP_OUTCOME_SKINS.CUSTOM.eq((byte) 1))
                .fetch();
    }

    /**
     * Gets {@link TradeUpOutcomeSkins} as {@link VOutSkins}.
     *
     * @param id tradeUp id
     * @return {@code Result<Record>} {@link VOutSkinsRecord}
     */
    public static Result<Record> getOutSkins(int id, byte custom){
        return dsl.select()
                .from(V_OUT_SKINS)
                .where(V_OUT_SKINS.TRADE_UP_ID.eq(id))
                .and(V_OUT_SKINS.CUSTOM.eq(custom))
                .fetch();
    }

    /**
     * Get {@link TradeUpOutcomeSkins} id by name cond tup custom.
     *
     * @param weapon    weapon
     * @param title     title
     * @param condition {@link Condition} condition
     * @param tupId     tradeUp id
     * @param custom    custom
     * @return {@link CS2Skin} id
     */
    public static int getOutSkinIdByNameCondTup(String weapon, String title, Condition condition, int tupId, boolean custom){
        Integer id =  dsl.select(V_OUT_SKINS.ID)
                .from(V_OUT_SKINS)
                .where(V_OUT_SKINS.TRADE_UP_ID.eq(tupId))
                .and(V_OUT_SKINS.CUSTOM.eq((byte) (custom ? 1:0)))
                .and(V_OUT_SKINS.WEAPON.eq(weapon))
                .and(V_OUT_SKINS.TITLE.eq(title))
                .and(V_OUT_SKINS.CONDITION.eq(condition))
                .fetchOneInto(Integer.class);
        if (id == null)
            throw new RuntimeException("Couldn't get SkinId for -> " + weapon + title + " condition -> " + condition + " tupId -> " + tupId);

        return id;
    }

    /**
     * Gets skin-info from {@link TradeUpSkins}.
     * <p>WEAPON,TITLE, COLL_NAME, RARITY, STATTRAK, CONDITION</p>
     *
     * @param custom custom
     * @param tupId  tradeUp id
     * @return infos as {@code Result<Record6>} WEAPON,TITLE, COLL_NAME, RARITY, STATTRAK, CONDITION
     */
    public static Result<Record6<String, String, String, Rarity, Byte, Condition>> getTradeUpSkinInfo(byte custom,int tupId) {
        return dsl.selectDistinct(V_FULLCS2SKIN.WEAPON,V_FULLCS2SKIN.TITLE, V_FULLCS2SKIN.COLL_NAME, V_FULLCS2SKIN.RARITY, V_FULLCS2SKIN.STATTRAK, V_FULLCS2SKIN.CONDITION)
                .from(TRADE_UP_SKINS)
                .join(V_FULLCS2SKIN)
                .on(TRADE_UP_SKINS.C_S2_SKIN_ID.eq(V_FULLCS2SKIN.ID))
                .where(TRADE_UP_SKINS.TRADE_UP_ID.eq(tupId))
                .and(TRADE_UP_SKINS.CUSTOM.eq(custom)).fetch();
    }

    /**
     * Get {@link CS2Skin} ids,condition,stat by name.
     *
     * @param weapon weapon
     * @param title  title
     * @return {@code Result<Record3>} id,condition,stattrak
     */
    public static Result<Record3<Integer, Condition, Byte>> getSkinIdsCondStatByName(String weapon, String title){
        return dsl.select(C_S2_SKIN.ID, C_S2_SKIN.CONDITION, C_S2_SKIN.STATTRAK)
                .from(STASH_SKIN_HOLDER)
                .join(C_S2_SKIN)
                .on(STASH_SKIN_HOLDER.STASH_ID.eq(C_S2_SKIN.STASH_ID))
                .where(STASH_SKIN_HOLDER.WEAPON.eq(weapon))
                .and(STASH_SKIN_HOLDER.TITLE.eq(title))
                .fetch();
        }

    /**
     * Delete custom {@link TradeUpSkins} & {@link TradeUpOutcomeSkins} for a custom {@link TradeUp}.
     *
     * @param id custom tradeUp id
     */
    public static void deleteCustomInAndOutSkins(int id){
        dsl.deleteFrom(TRADE_UP_SKINS).where(TRADE_UP_SKINS.TRADE_UP_ID.eq(id)).and(TRADE_UP_SKINS.CUSTOM.eq((byte) 1)).execute();
        dsl.deleteFrom(TRADE_UP_OUTCOME_SKINS).where(TRADE_UP_OUTCOME_SKINS.TRADE_UP_ID.eq(id)).and(TRADE_UP_OUTCOME_SKINS.CUSTOM.eq((byte) 1)).execute();
    }

}