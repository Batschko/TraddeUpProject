package de.batschko.tradeupproject.db.query;


import de.batschko.tradeupproject.enums.Condition;
import de.batschko.tradeupproject.enums.Rarity;
import de.batschko.tradeupproject.tables.SkinPrice;
import org.jooq.*;
import org.springframework.stereotype.Repository;

import static de.batschko.tradeupproject.tables.TradeUpSkins.TRADE_UP_SKINS;
import static de.batschko.tradeupproject.tables.VFullcs2skinCsmoney.V_FULLCS2SKIN_CSMONEY;

/**
 * Database access related to {@link SkinPrice}.
 */
@Repository
public class QRCSMoney extends QueryRepository{

    public QRCSMoney(DSLContext dslContext) {
        super(dslContext);
    }


    public static Result<Record6<String, String, String, Rarity, Byte, Condition>> getTradeUpSkinInfo(int tupId) {
         Result<Record6<String, String, String, Rarity, Byte, Condition>> result =
                dsl.selectDistinct(V_FULLCS2SKIN_CSMONEY.WEAPON,V_FULLCS2SKIN_CSMONEY.TITLE, V_FULLCS2SKIN_CSMONEY.COLL_NAME, V_FULLCS2SKIN_CSMONEY.RARITY, V_FULLCS2SKIN_CSMONEY.STATTRAK, V_FULLCS2SKIN_CSMONEY.CONDITION)
                        .from(TRADE_UP_SKINS)
                        .join(V_FULLCS2SKIN_CSMONEY)
                        .on(TRADE_UP_SKINS.C_S2_SKIN_ID.eq(V_FULLCS2SKIN_CSMONEY.ID))
                        .where(TRADE_UP_SKINS.TRADE_UP_ID.eq(tupId)).fetch();

        return result;
    }

    }
