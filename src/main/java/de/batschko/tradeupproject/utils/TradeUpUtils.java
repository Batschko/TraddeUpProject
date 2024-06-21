package de.batschko.tradeupproject.utils;

import de.batschko.tradeupproject.db.customtable.TradeUpCustom;
import de.batschko.tradeupproject.db.query.QRCSMoneyPrice;
import de.batschko.tradeupproject.db.query.QRTradeUp;
import de.batschko.tradeupproject.webfetchers.CSMoneyScraper;
import de.batschko.tradeupproject.webfetchers.CSMoneyWiki;
import lombok.extern.slf4j.Slf4j;
import org.jooq.Record4;
import org.jooq.Result;

import java.util.List;

@Slf4j
public class TradeUpUtils {

    /**
     * Calculates all trade ups using CSMoney price.
     * <p>Gets all TradeUps which are not calculated from db and calculates them</p>
     */
    public static void calculateAllTradeUps(){
        List<TradeUpCustom> tradeUps = QRTradeUp.getTradeUpsToCalculate();
        for(TradeUpCustom tup : tradeUps){
            tup.setCalculation(true);
        }
    }
    public static void reCalculateTradeUps() {
        List<TradeUpCustom> tupList = QRTradeUp.getTradeUpList();
        TradeUpCustom.reCalculateUpdatedPrices(tupList);
    }

    public static void priceUpdateMissing(){
        fullPriceUpdate(false);
    }
    public static void priceUpdateByDate(){
        fullPriceUpdate(true);
    }

    private static void fullPriceUpdate(boolean byDate){
        int lastSize = 0;
        while (true){
            Result<Record4<String, String, Double, Double>> nameList;
            if(byDate) nameList = QRCSMoneyPrice.getCSMoneyPriceListByDate();
            else nameList = QRCSMoneyPrice.getCSMoneyPriceListMissing();

            if(nameList.size() == lastSize){
                break;
            }else {
                CSMoneyScraper.updatePrice(nameList);
            }
            lastSize = nameList.size();
        }
        List<Integer> ids;
        if(byDate) ids = QRCSMoneyPrice.getCSMoneyPriceListByDateId();
        else ids = QRCSMoneyPrice.getCSMoneyPriceListMissingIds();

        log.info("\n\n\nget remaining by CSMoney Bot");
        log.info("updating {} names", ids.size());
        int loop = 1;
        for(int id : ids){
            log.info("loop: "+loop++);
            CSMoneyWiki.updateCSMoneySkinPrice(id);
            try {
                Thread.sleep(3500);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }



}
