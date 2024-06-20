package de.batschko.tradeupproject.db.query;


import de.batschko.tradeupproject.enums.Condition;
import de.batschko.tradeupproject.enums.Rarity;
import org.jooq.*;
import org.springframework.stereotype.Repository;

import static de.batschko.tradeupproject.tables.TradeUpSkins.TRADE_UP_SKINS;
import static de.batschko.tradeupproject.tables.VFullcs2skin.V_FULLCS2SKIN;
/**
 * Database access related to {@link }.
 */
@Repository
public class QRCSMoney extends QueryRepository{

    public QRCSMoney(DSLContext dslContext) {
        super(dslContext);
    }


    public static Result<Record6<String, String, String, Rarity, Byte, Condition>> getTradeUpSkinInfo(int tupId) {
         Result<Record6<String, String, String, Rarity, Byte, Condition>> result =
                dsl.selectDistinct(V_FULLCS2SKIN.WEAPON,V_FULLCS2SKIN.TITLE, V_FULLCS2SKIN.COLL_NAME, V_FULLCS2SKIN.RARITY, V_FULLCS2SKIN.STATTRAK, V_FULLCS2SKIN.CONDITION)
                        .from(TRADE_UP_SKINS)
                        .join(V_FULLCS2SKIN)
                        .on(TRADE_UP_SKINS.C_S2_SKIN_ID.eq(V_FULLCS2SKIN.ID))
                        .where(TRADE_UP_SKINS.TRADE_UP_ID.eq(tupId)).fetch();

        return result;
    }

    }
