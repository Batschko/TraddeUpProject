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



    public static double getSkinPrice(int cs2skinId){
        Double result = dsl.select(C_S2_SKIN.PRICE)
                .from(C_S2_SKIN)
                .where(C_S2_SKIN.ID.eq(cs2skinId))
                .fetchOneInto(Double.class);
        if(result == null) result=  0.0;//throw new RuntimeException("Couldn't get SkinPrice for cs2skinId: "+cs2skinId); //result=  -1.0;//throw new RuntimeException("Couldn't get SkinPrice for cs2skinId: "+cs2skinId);
        return result;
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
