package de.batschko.tradeupproject.db.query;


import de.batschko.tradeupproject.enums.Condition;
import lombok.extern.slf4j.Slf4j;
import org.jooq.DSLContext;
import org.jooq.Record4;
import org.jooq.Result;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static de.batschko.tradeupproject.tables.CS2Skin.C_S2_SKIN;
import de.batschko.tradeupproject.tables.CS2Skin;
import static de.batschko.tradeupproject.tables.StashSkinHolder.STASH_SKIN_HOLDER;
import static org.jooq.impl.DSL.localDateTime;

/**
 * Database access related to {@link CS2Skin } prices.
 */
@Repository
@Slf4j
public class QRSkinPrice extends QueryRepository{

    public QRSkinPrice(DSLContext dslContext) {
        super(dslContext);
    }


    public static Result<Record4<String, String, Double, Double>> getSkinPriceList() {
        return dsl.selectDistinct(STASH_SKIN_HOLDER.WEAPON, STASH_SKIN_HOLDER.TITLE, STASH_SKIN_HOLDER.FLOAT_START, STASH_SKIN_HOLDER.FLOAT_END)
                        .from(STASH_SKIN_HOLDER)
                        .join(C_S2_SKIN)
                        .on(C_S2_SKIN.STASH_ID.eq(STASH_SKIN_HOLDER.STASH_ID))
                        .orderBy(C_S2_SKIN.ID.asc()).fetch();

    }

    public static Result<Record4<String, String, Double, Double>> getSkinPriceListByDate() {
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

    public static List<Integer> getSkinPriceListByDateId() {

        final int minusHours = 12;
        DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss");
        String time = dateFmt.format(ZonedDateTime.now(ZoneId.of("Europe/Berlin")).minusHours(minusHours));
        return dsl.selectDistinct(C_S2_SKIN.ID)
                        .from(C_S2_SKIN)
                        .where(C_S2_SKIN.MODIFIED_DATE.lt(localDateTime(time)))
                        .orderBy(C_S2_SKIN.ID.asc())
                        .fetchInto(Integer.class);
    }

    public static Result<Record4<String, String, Double, Double>> getSkinPriceListMissing() {
        return dsl.selectDistinct(STASH_SKIN_HOLDER.WEAPON, STASH_SKIN_HOLDER.TITLE, STASH_SKIN_HOLDER.FLOAT_START, STASH_SKIN_HOLDER.FLOAT_END)
                        .from(STASH_SKIN_HOLDER)
                        .join(C_S2_SKIN)
                        .on(C_S2_SKIN.STASH_ID.eq(STASH_SKIN_HOLDER.STASH_ID))
                        .where(C_S2_SKIN.PRICE.isNull())
                        .orderBy(C_S2_SKIN.ID.asc()).fetch();
    }

    public static List<Integer> getSkinPriceListMissingIds() {
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


}
