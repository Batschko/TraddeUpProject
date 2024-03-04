package de.batschko.tradeupproject.db.query;


import de.batschko.tradeupproject.enums.PriceType;
import de.batschko.tradeupproject.tables.CS2Skin;
import de.batschko.tradeupproject.tables.SkinPrice;
import de.batschko.tradeupproject.tables.records.SkinPriceRecord;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import static de.batschko.tradeupproject.tables.CS2Skin.C_S2_SKIN;
import static de.batschko.tradeupproject.tables.SkinPrice.SKIN_PRICE;

/**
 * Database access related to {@link SkinPrice}.
 */
@Repository
public class QRSkinPrice extends QueryRepository{

    public QRSkinPrice(DSLContext dslContext) {
        super(dslContext);
    }



    /**
     * Get price for {@link CS2Skin}.
     *
     * @param cs2skinId cs2skin id
     * @return the skin price
     */
    public static double getSkinPrice(int cs2skinId){
        Double result = dsl.select(SKIN_PRICE.PRICE)
                .from(SKIN_PRICE)
                .join(C_S2_SKIN)
                .on(C_S2_SKIN.SKIN_PRICE_ID.eq(SKIN_PRICE.ID))
                .where(C_S2_SKIN.ID.eq(cs2skinId))
                .fetchOneInto(Double.class);
        if(result == null) throw new RuntimeException("Couldn't get SkinPrice for cs2skinId: "+cs2skinId);
        return result;
    }


    /**
     * Save {@link SkinPriceRecord} and return its id.
     *
     * @param priceType  price type {@link PriceType}
     * @param price      price
     * @param amountSold amount sold
     * @return saved {@link SkinPriceRecord} id
     */
    public static int save(PriceType priceType, double price, int amountSold) {
        SkinPriceRecord test_obj = dsl.newRecord(SkinPrice.SKIN_PRICE);
        test_obj.setPriceType(priceType);
        test_obj.setPrice(price);
        test_obj.setAmountSold(amountSold);

        test_obj.store();
        return  test_obj.getId();
    }



    }
