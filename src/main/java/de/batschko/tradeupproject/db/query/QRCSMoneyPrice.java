package de.batschko.tradeupproject.db.query;


import de.batschko.tradeupproject.enums.Condition;
import de.batschko.tradeupproject.enums.Rarity;
import de.batschko.tradeupproject.enums.TradeUpStatus;

import de.batschko.tradeupproject.tables.TradeUp;

import de.batschko.tradeupproject.tradeup.TradeUpSettings;
import de.batschko.tradeupproject.utils.SkinUtils;
import lombok.extern.slf4j.Slf4j;
import org.jooq.Record;
import org.jooq.*;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static de.batschko.tradeupproject.tables.CS2Skin.C_S2_SKIN;

import static de.batschko.tradeupproject.tables.StashSkinHolder.STASH_SKIN_HOLDER;
import static de.batschko.tradeupproject.tables.TradeUp.TRADE_UP;
import static de.batschko.tradeupproject.tables.TradeUpOutcomeSkins.TRADE_UP_OUTCOME_SKINS;
import static de.batschko.tradeupproject.tables.TradeUpSkins.TRADE_UP_SKINS;
import static de.batschko.tradeupproject.tables.VTupnsettinggs.V_TUPNSETTINGGS;
import static de.batschko.tradeupproject.tables.VFullcs2skin.V_FULLCS2SKIN;

import static org.jooq.impl.DSL.*;

/**
 * Database access related to {@link }.
 */
@Repository
@Slf4j
public class QRCSMoneyPrice extends QueryRepository{

    public QRCSMoneyPrice(DSLContext dslContext) {
        super(dslContext);
    }





    public static Result<Record4<String, String, Double, Double>> getCSMoneyPriceList() {
        return dsl.selectDistinct(STASH_SKIN_HOLDER.WEAPON, STASH_SKIN_HOLDER.TITLE, STASH_SKIN_HOLDER.FLOAT_START, STASH_SKIN_HOLDER.FLOAT_END)
                        .from(STASH_SKIN_HOLDER)
                        .join(C_S2_SKIN)
                        .on(C_S2_SKIN.STASH_ID.eq(STASH_SKIN_HOLDER.STASH_ID))
                        .orderBy(C_S2_SKIN.ID.asc()).fetch();

    }

    public static Result<Record4<String, String, Double, Double>> getCSMoneyPriceListByDate() {
        final int minusHours = 24;
        DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss");
        String time = dateFmt.format(ZonedDateTime.now(ZoneId.of("Europe/Berlin")).minusHours(minusHours));
        return dsl.selectDistinct(STASH_SKIN_HOLDER.WEAPON, STASH_SKIN_HOLDER.TITLE, STASH_SKIN_HOLDER.FLOAT_START, STASH_SKIN_HOLDER.FLOAT_END)
                        .from(STASH_SKIN_HOLDER)
                        .join(C_S2_SKIN)
                        .on(C_S2_SKIN.STASH_ID.eq(STASH_SKIN_HOLDER.STASH_ID))
                        .where(C_S2_SKIN.MODIFIED_DATE.lt(localDateTime(time)))
                        .orderBy(C_S2_SKIN.ID.asc()).fetch();
    }

    public static List<Integer> getCSMoneyPriceListByDateId() {

        final int minusHours = 12;
        DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss");
        String time = dateFmt.format(ZonedDateTime.now(ZoneId.of("Europe/Berlin")).minusHours(minusHours));
        return dsl.selectDistinct(C_S2_SKIN.ID)
                        .from(C_S2_SKIN)
                        .where(C_S2_SKIN.MODIFIED_DATE.lt(localDateTime(time)))
                        .orderBy(C_S2_SKIN.ID.asc())
                        .fetchInto(Integer.class);
    }

    public static Result<Record4<String, String, Double, Double>> getCSMoneyPriceListMissing() {
        return dsl.selectDistinct(STASH_SKIN_HOLDER.WEAPON, STASH_SKIN_HOLDER.TITLE, STASH_SKIN_HOLDER.FLOAT_START, STASH_SKIN_HOLDER.FLOAT_END)
                        .from(STASH_SKIN_HOLDER)
                        .join(C_S2_SKIN)
                        .on(C_S2_SKIN.STASH_ID.eq(STASH_SKIN_HOLDER.STASH_ID))
                        .where(C_S2_SKIN.PRICE.isNull())
                        .orderBy(C_S2_SKIN.ID.asc()).fetch();
    }

    public static List<Integer> getCSMoneyPriceListMissingIds() {
        return dsl.select(C_S2_SKIN.ID)
                        .from(C_S2_SKIN)
                        .where(C_S2_SKIN.PRICE.isNull())
                        .orderBy(C_S2_SKIN.ID.asc()).fetchInto(Integer.class);
    }

    public static void update(String weapon, String title, Condition condition, byte stat , double price) {

        Integer skinId = dsl.select(C_S2_SKIN.ID).from(C_S2_SKIN).join(STASH_SKIN_HOLDER)
                .on(C_S2_SKIN.STASH_ID.eq(STASH_SKIN_HOLDER.STASH_ID))
                .where(STASH_SKIN_HOLDER.WEAPON.eq(weapon))
                .and(STASH_SKIN_HOLDER.TITLE.eq(title))
                .and(C_S2_SKIN.CONDITION.eq(condition))
                .and(C_S2_SKIN.STATTRAK.eq(stat))
                .fetchOneInto(Integer.class);

        //skip if condition for skins doesn't exist
        if(skinId == null){
            return;
        }
        dsl.update(C_S2_SKIN).set(C_S2_SKIN.PRICE, price).where(C_S2_SKIN.ID.eq(skinId)).execute();
    }

    public static void update(int id, double price) {
        dsl.update(C_S2_SKIN).set(C_S2_SKIN.PRICE, price).set(C_S2_SKIN.MODIFIED_DATE, LocalDateTime.now(ZoneId.of("Europe/Berlin"))).where(C_S2_SKIN.ID.eq(id)).execute();
    }

  /*  public static void save(String weapon, String title, Condition condition, byte stat , double price, int amount){
        Integer skinId = dsl.select(C_S2_SKIN.ID).from(C_S2_SKIN).join(STASH_SKIN_HOLDER)
                .on(C_S2_SKIN.STASH_ID.eq(STASH_SKIN_HOLDER.STASH_ID))
                .where(STASH_SKIN_HOLDER.WEAPON.eq(weapon))
                .and(STASH_SKIN_HOLDER.TITLE.eq(title))
                .and(C_S2_SKIN.CONDITION.eq(condition))
                .and(C_S2_SKIN.STATTRAK.eq(stat))
                .fetchOneInto(Integer.class);

        //skip if condition for skins doesnt exist
        if(skinId == null){
            return;
        }

        CSMoneyPriceRecord newRecord = dsl.newRecord(CSMoneyPrice.C_S_MONEY_PRICE);
        newRecord.setCS2SkinId(skinId);
        newRecord.setPrice(price);
        newRecord.setAmount(amount);
        try{
            newRecord.store();
        }catch (Exception e){
            log.warn("Saving CSMoneyPrice failed for {} | {}  id:{}",weapon,title,skinId);
        }

    }*/

    public static double getSkinPrice(int cs2skinId){
        Double result = dsl.select(C_S2_SKIN.PRICE)
                .from(C_S2_SKIN)
                .where(C_S2_SKIN.ID.eq(cs2skinId))
                .fetchOneInto(Double.class);
        if(result == null) result=  0.0;//throw new RuntimeException("Couldn't get SkinPrice for cs2skinId: "+cs2skinId); //result=  -1.0;//throw new RuntimeException("Couldn't get SkinPrice for cs2skinId: "+cs2skinId);
        return result;
    }


    @Deprecated
    public static double getTradeUpSkinAverageAmountSold(String collName, Condition condition, int tupId) {
    /*    Double result = dsl.select(avg(V_FULLCS2SKIN.AMOUNT))
                .from(V_FULLCS2SKIN)
                .join(TRADE_UP_SKINS)
                .on(TRADE_UP_SKINS.C_S2_SKIN_ID.eq(V_FULLCS2SKIN.ID))
                .where(V_FULLCS2SKIN.COLL_NAME.eq(collName))
                .and(V_FULLCS2SKIN.CONDITION.eq(condition))
                .and(TRADE_UP_SKINS.TRADE_UP_ID.eq(tupId))
                .fetchOneInto(Double.class);
        if (result==null){
            return -3;
        }
        return result;*/
        return -1;
    }

    public static double getTradeUpSkinAverageAmountSoldCustom(String collName, Condition condition, int tupId) {
     /*   Double result = dsl.select(avg(V_FULLCS2SKIN.AMOUNT))
                .from(V_FULLCS2SKIN)
                .join(TRADE_UP_SKINS_CUSTOM)
                .on(TRADE_UP_SKINS_CUSTOM.C_S2_SKIN_ID.eq(V_FULLCS2SKIN.ID))
                .where(V_FULLCS2SKIN.COLL_NAME.eq(collName))
                .and(V_FULLCS2SKIN.CONDITION.eq(condition))
                .and(TRADE_UP_SKINS_CUSTOM.TRADE_UP_CUSTOM_ID.eq(tupId))
                .fetchOneInto(Double.class);
        if (result==null){
            return -3;
        }
        return result;*/
        return -1;
    }


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
            log.warn("Couldn't get average TradeUpSkins price for: tupId -> " + tupId + " coll -> " + collName + " cond -> " + condition);
            return -3;
        }
        return result;
    }


    public static double getTradeUpSkinsAveragePriceCustom(String collName, Condition condition, int tupId) {
/*
        Double result = dsl.select(avg(V_FULLCS2SKIN.PRICE))
                .from(V_FULLCS2SKIN)
                .join(TRADE_UP_SKINS_CUSTOM)
                .on(TRADE_UP_SKINS_CUSTOM.C_S2_SKIN_ID.eq(V_FULLCS2SKIN.ID))
                .where(V_FULLCS2SKIN.COLL_NAME.eq(collName))
                .and(V_FULLCS2SKIN.CONDITION.eq(condition))
                .and(TRADE_UP_SKINS_CUSTOM.TRADE_UP_CUSTOM_ID.eq(tupId))
                .fetchOneInto(Double.class);
        if (result==null){
            log.warn("Couldn't get average TradeUpSkins price for: tupId -> " + tupId + " coll -> " + collName + " cond -> " + condition);
            return -3;
        }
        return result;*/
        return -1;
    }




    public static void createTradeUpSkins(){
        Result<Record> tupAndSettings = getTradeUpAndSettingsBuilder().fetch();
        processCreateTradeUpSkins(tupAndSettings, false);
    }



    private static SelectConditionStep<Record> getTradeUpAndSettingsBuilder(){
        return dsl.select()
                .from(V_TUPNSETTINGGS)
                .leftJoin(TRADE_UP_SKINS)
                .on(V_TUPNSETTINGGS.ID.eq(TRADE_UP_SKINS.TRADE_UP_ID))
                .where(TRADE_UP_SKINS.TRADE_UP_ID.isNull());
    }



    //TODO check min_price for normal calcuation because -1 etc.
    private static void processCreateTradeUpSkins(Result<Record> tupAndSettings, boolean deleteTradeUps){
        tradeupLoop:
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
                //TODO delete tradeups that are not possible beause condition doesnt exist?
                List<Row2<Integer, Integer>> rowList = tupSkinIds.stream().map(tupSkinId -> row(tupId, tupSkinId)).toList();
                if(rowList.isEmpty()){
                    //todo
                    log.warn(""+settings + " "+rarity+ " "+ stat);
                    if(deleteTradeUps){
                        int deleted = dsl.deleteFrom(TradeUp.TRADE_UP).where(TradeUp.TRADE_UP.ID.eq(tupId)).execute();
                        //TODO should be already deleted
                        if(deleted!=1){
                            log.warn("Error deleting TradeUp: "+tupId);
                        }
                        log.info("Deleted TradeUP: "+tupId);
                        continue tradeupLoop;
                    }
                }

                try{
                    int inserted = dsl.insertInto(TRADE_UP_SKINS, TRADE_UP_SKINS.TRADE_UP_ID, TRADE_UP_SKINS.C_S2_SKIN_ID).valuesOfRows(rowList).execute();
                    if(inserted != tupSkinIds.size()){
                        throw new RuntimeException("Error inserting TradeUpSkins");
                    }
                }catch (Exception e){
                    throw new RuntimeException("Exception inserting TradeUpSkins");
                }
            }
        }
    }

    public static Result<Record3<Integer, Condition, Byte>> getSkinIdsFromName(String weapon, String title){
        return dsl.select(C_S2_SKIN.ID, C_S2_SKIN.CONDITION, C_S2_SKIN.STATTRAK)
                .from(STASH_SKIN_HOLDER)
                .join(C_S2_SKIN)
                .on(STASH_SKIN_HOLDER.STASH_ID.eq(C_S2_SKIN.STASH_ID))
                .where(STASH_SKIN_HOLDER.WEAPON.eq(weapon))
                .and(STASH_SKIN_HOLDER.TITLE.eq(title))
                .fetch();

    }




    }
