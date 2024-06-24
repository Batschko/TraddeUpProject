package de.batschko.tradeupproject.utils;

import de.batschko.tradeupproject.db.customtable.CS2SkinCustom;
import de.batschko.tradeupproject.db.query.QRSkinPrice;
import de.batschko.tradeupproject.db.query.QRStashHolder;
import de.batschko.tradeupproject.db.query.QRTradeUpGenerated;
import de.batschko.tradeupproject.enums.Condition;
import de.batschko.tradeupproject.enums.Rarity;
import de.batschko.tradeupproject.tables.CS2Skin;
import de.batschko.tradeupproject.tables.StashSkinHolder;
import de.batschko.tradeupproject.webfetchers.CSMoneyScraper;
import de.batschko.tradeupproject.webfetchers.CSMoneyWiki;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.jooq.Record4;
import org.jooq.Result;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static de.batschko.tradeupproject.tables.Collection.COLLECTION;
import static de.batschko.tradeupproject.tables.StashSkinHolder.STASH_SKIN_HOLDER;


/**
 * Utility methods and classes related to {@link CS2Skin}
 */
@Slf4j
public class SkinUtils {


    /**
     * Get full skin name from details
     *
     * @param stattrak  stattrak
     * @param weapon    weapon
     * @param title     title
     * @param condition condition
     * @return full skin name
     */
    private static String getFullSkinName(byte stattrak, String weapon, String title, Condition condition){
        String fullName = "";
        if(stattrak == 1){
            fullName="StatTrak™ ";
        }
        fullName=fullName+weapon+" | "+title+" ("+condition.getText()+")";
        return fullName;
    }


    /**
     * Generate list of all cs2 skins
     * Uses {@link StashSkinHolder} to generate full skins
     * @return list of {@link CS2SkinCustom}
     */
    public static List<CS2SkinCustom> generateCS2Skins(){
        log.info("Generating CS2 skins....");
        Result<Record4<Integer, Double, Double, Byte>> result = QRStashHolder.getCS2SkinInfo();
        int stashId;
        double floatStart, floatEnd;
        boolean isCase;
        List<Condition> conditions;
        List<CS2SkinCustom> cs2SkinList = new ArrayList<>();
        for (Record4<Integer, Double, Double, Byte> record : result) {
            stashId = record.get(STASH_SKIN_HOLDER.STASH_ID);
            floatStart = record.get(STASH_SKIN_HOLDER.FLOAT_START);
            floatEnd = record.get(STASH_SKIN_HOLDER.FLOAT_END);
            isCase = record.get(COLLECTION.IS_CASE)==1;

            conditions = Condition.getPossibleConditions(floatStart, floatEnd);

            for(Condition cond : conditions){
                cs2SkinList.add(new CS2SkinCustom(stashId, (byte) 0, cond));
                if(isCase){
                    cs2SkinList.add(new CS2SkinCustom(stashId, (byte) 1, cond));
                }
            }
        }
        return cs2SkinList;
    }

    /**
     * Get special skin names as map.
     * <p>Default wrapper for {@link #getSpecialSkinNamesMap(boolean)} reverse: false</p>
     * @return the map with special names mapped to normalized names
     */
    public static Map<String,String> getSpecialSkinNamesMap(){
        return getSpecialSkinNamesMap(false);
    }

    /**
     * Get special skin names as map.
     * Default key=special name value=normalized name
     * @param reverse reverse key an values
     * @return the map with special names mapped to normalized names based on reverse
     */
    public static Map<String,String> getSpecialSkinNamesMap(boolean reverse){
        String fileName = "src/main/java/de/batschko/tradeupproject/db/SkinSpecialNames.txt";
        return readSpecialNameFile(fileName, reverse);
    }

    /**
     * Get special CSMoneyWiki names as map.
     * <p>Default wrapper for {@link #getCSMoneyWikiSpecialNames(boolean)} reverse: false</p>
     * @return the map with special names mapped to normalized names
     */
    public static Map<String,String> getCSMoneyWikiSpecialNames(){
        String fileName = "src/main/java/de/batschko/tradeupproject/db/CSMWikiSpecialNames.txt";
        return readSpecialNameFile(fileName, false);
    }

    /**
     * Get special CSMoneyWiki names as map.
     * Default key=special name value=normalized name
     * @param reverse reverse key an values
     * @return the map with special names mapped to normalized names based on reverse
     */
    public static Map<String,String> getCSMoneyWikiSpecialNames(boolean reverse){
        String fileName = "src/main/java/de/batschko/tradeupproject/db/CSMWikiSpecialNames.txt";
        return readSpecialNameFile(fileName, reverse);
    }

    /**
     * Update {@link CS2Skin} prices which are missing.
     */
    public static void priceUpdateMissing(){
        fullPriceUpdate(false);
    }

    /**
     * Update {@link CS2Skin} prices which are older than 24h.
     */
    public static void priceUpdateByDate(){
        fullPriceUpdate(true);
    }

    private static void fullPriceUpdate(boolean byDate){
        int lastSize = 0;
        while (true){
            Result<Record4<String, String, Double, Double>> nameList;
            if(byDate) nameList = QRSkinPrice.getSkinPriceListByDate();
            else nameList = QRSkinPrice.getSkinPriceListMissing();

            if(nameList.size() == lastSize){
                break;
            }else {
                CSMoneyScraper.updateSkinPrice(nameList);
            }
            lastSize = nameList.size();
        }
        List<Integer> ids;
        if(byDate) ids = QRSkinPrice.getSkinPriceListByDateId();
        else ids = QRSkinPrice.getSkinPriceListMissingIds();

        log.info("\n\n\nget remaining by CSMoney Bot");
        log.info("updating {} names", ids.size());
        int loop = 1;
        for(int id : ids){
            log.info("loop: "+loop++);
            CSMoneyWiki.updateSkinPrice(id);
            try {
                Thread.sleep(3500);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static Map<String,String> readSpecialNameFile(String fileName, boolean reverse){
        Map<String, String> map = new HashMap<>();
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(fileName, StandardCharsets.UTF_8));
            String line;
            while ((line = bufferedReader.readLine()) != null)
            {
                String[] tok = line.split(",");
                if(reverse){
                    map.put(tok[1], tok[0]);
                }else {
                    map.put(tok[0], tok[1]);
                }

            }
            bufferedReader.close();
        } catch (IOException e) {
            throw new RuntimeException("couldn't read file: "+fileName);
        }
        return map;
    }



    /*-------------*/
    /*   CLASSES   */
    /*-------------*/


    /**
     * Used as Data Transfer Object
     */
    @Getter
    public static class TradeUpSkinInfo extends SkinUtils{

        String coll_name;
        Condition condition;
        Rarity rarity;
        byte stattrak;


        /**
         * Instantiates a new {@link TradeUpSkinInfo}.
         *
         * @param coll_name the collection name
         * @param condition the condition {@link Condition}
         * @param rarity    the rarity {@link Rarity}
         * @param stattrak  stattrak as byte
         */
        public TradeUpSkinInfo(String coll_name, Condition condition, Rarity rarity, byte stattrak) {
            this.coll_name = coll_name;
            this.condition = condition;
            this.rarity = rarity;
            this.stattrak = stattrak;
        }

        @Override
        public String toString() {
            return "TradeUpSkinInfo{" +
                    "coll_name='" + coll_name + '\'' +
                    ", condition=" + condition +
                    ", rarity=" + rarity +
                    ", stattrak=" + stattrak +
                    '}';
        }
    }

    /**
     * Used as Data Transfer Object
     */
    @Getter
    public static class SkinFullName extends SkinUtils {
        int id;
        Byte stattrak;
        String weapon;
        String title;
        Condition condition;

        /**
         * Instantiates a new {@link SkinFullName}.
         *
         * @param id        skin id
         * @param stattrak  stattrak as byte
         * @param weapon    weapon text
         * @param title     title text
         * @param condition skin condition {@link Condition}
         */
        public SkinFullName(int id, Byte stattrak, String weapon, String title, Condition condition) {
            this.id = id;
            this.stattrak = stattrak;
            this.weapon = weapon;
            this.title = title;
            this.condition = condition;
        }


        /**
         * Get full skin name.
         *
         * @return full skin name
         */
        public String getFullName(){
            return getFullSkinName(this.stattrak, this.weapon, this.title, this.condition);
        }

        /**
         * Get full skin name for special char names.
         *
         * @param newTitle the new title without special chars
         * @return full skin name
         */
        public String getFullNameSpecialChars(String newTitle){
            return getFullSkinName(this.stattrak, this.weapon, newTitle, this.condition);
        }

        @Override
        public String toString() {
            return "SkinFullName{" +
                    "skin_id=" + id +
                    ", stattrak=" + stattrak +
                    ", weapon='" + weapon + '\'' +
                    ", title='" + title + '\'' +
                    ", condition=" + condition +
                    '}';
        }
    }
}

