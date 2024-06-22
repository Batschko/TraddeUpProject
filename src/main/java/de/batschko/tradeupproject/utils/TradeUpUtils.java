package de.batschko.tradeupproject.utils;

import de.batschko.tradeupproject.db.customtable.TradeUpCustom;
import de.batschko.tradeupproject.db.query.QRTradeUpGenerated;
import de.batschko.tradeupproject.tables.CS2Skin;
import de.batschko.tradeupproject.tables.TradeUp;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * Utility methods related to {@link TradeUp}.
 */
@Slf4j
public class TradeUpUtils {

    /**
     * Calculates all TradeUps using CSMoney price.
     * <p>Gets all TradeUps which are not calculated from db and calculates them</p>
     */
    public static void calculateAllTradeUps(){
        List<TradeUpCustom> tradeUps = QRTradeUpGenerated.getTradeUpsToCalculate();
        log.info("calculating tups: {}", tradeUps.size());
        for(TradeUpCustom tup : tradeUps){
            tup.setCalculation(true);
        }
    }

    /**
     * Re-Calculates all TradeUps using CSMoney price.
     * <p>Use this after updating {@link CS2Skin} prices</p>
     */
    public static void reCalculateTradeUps() {
        List<TradeUpCustom> tupList = QRTradeUpGenerated.getTradeUpList();
        TradeUpCustom.reCalculateUpdatedPrices(tupList);
    }


}
