package de.batschko.tradeupproject.utils;

import de.batschko.tradeupproject.TradeUpProjectApplication;
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
     * Calculates all trade ups using backpack price.
     * <p>Gets all TradeUps which are not calculated from db and calculates them</p>
     */
    @Deprecated
    public static void calculateAllTradeUpsBackpackPrice(){
        List<TradeUpCustom> tradeUps = QRTradeUp.getTradeUpsToCalculate();
        for(TradeUpCustom tup : tradeUps){
            tup.setCalculation(false);
        }
    }

    /**
     * Calculates all trade ups using CSMoney price.
     * <p>Gets all TradeUps which are not calculated from db and calculates them</p>
     */
    public static void calculateAllTradeUpsCSMoneyPrice(){
        List<TradeUpCustom> tradeUps = QRTradeUp.getTradeUpsToCalculate();
        for(TradeUpCustom tup : tradeUps){
            tup.setCalculation(true);
        }
    }

    public static void fullPriceUpdate(){
        int lastSize = 0;
        while (true){
            Result<Record4<String, String, Double, Double>> nameList = QRCSMoneyPrice.getCSMoneyPriceListByDate();
            if(nameList.size() == lastSize){
                break;
            }else {
                CSMoneyScraper.updatePrice(nameList);
            }
            lastSize = nameList.size();
        }
        List<Integer> ids = QRCSMoneyPrice.getCSMoneyPriceListByDateId();
        log.info("\n\n\nget remaining by CSMoney Bot");
        log.info("updating {} names", ids.size());
        int loop = 1;
        for(int id : ids){
            log.info("loop: "+loop++);
            CSMoneyWiki.updateCSMoneySkinPrice(id);
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static void initFullPriceUpdate(boolean init){
        if(init){
            Result<Record4<String, String, Double, Double>> nameList = QRCSMoneyPrice.getCSMoneyPriceList();
            CSMoneyScraper.updatePrice(nameList);
        }
        int size = 0;
        while (true){
            Result<Record4<String, String, Double, Double>> nameList2 = QRCSMoneyPrice.getCSMoneyPriceListMissing();
            if(nameList2.size() == size){

                break;
            }else {
                CSMoneyScraper.updatePrice(nameList2);
            }
            size = nameList2.size();
        }
        List<Integer> ids = QRCSMoneyPrice.getCSMoneyPriceListMissingIds();
        for(int id : ids){
            CSMoneyWiki.updateCSMoneySkinPrice(id);
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    //todo
}
