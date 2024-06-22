package de.batschko.tradeupproject.db.query;

import de.batschko.tradeupproject.enums.Rarity;
import de.batschko.tradeupproject.tables.TradeUp;

import static de.batschko.tradeupproject.tables.TradeUpSkins.TRADE_UP_SKINS;
import static de.batschko.tradeupproject.tables.VFullcs2skin.V_FULLCS2SKIN;
import de.batschko.tradeupproject.tradeup.TradeUpSettings;
import de.batschko.tradeupproject.utils.SkinUtils;
import lombok.extern.slf4j.Slf4j;
import org.jooq.*;
import org.springframework.stereotype.Repository;

import java.util.List;

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
