package de.batschko.tradeupproject.utils;

import de.batschko.tradeupproject.db.customtable.CS2SkinCustom;
import de.batschko.tradeupproject.db.query.QRCS2Skin;
import de.batschko.tradeupproject.db.query.QRSkinPrice;
import de.batschko.tradeupproject.db.query.QRStashHolder;
import de.batschko.tradeupproject.enums.Condition;
import de.batschko.tradeupproject.enums.PriceType;
import de.batschko.tradeupproject.enums.Rarity;
import de.batschko.tradeupproject.tables.StashSkinHolder;
import de.batschko.tradeupproject.webfetchers.CSGOBackpackApi;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.jooq.Record4;
import org.jooq.Result;
import org.json.JSONObject;
import org.json.JSONTokener;
//import org.json.parser.JSONParser;

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
 * Utility methods and classes for Skins
 */
@Slf4j
public class SkinUtils {


    /**
     * Get full skin name from details
     *
     * @param stattrak  the stattrak
     * @param weapon    the weapon
     * @param title     the title
     * @param condition the condition
     * @return the string
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


    /**
     * Sets skin prices with limit.
     *<p>switch between 'percent20InsteadOfPlus' to get a higher api request limit</p>
     * @param limit                  the limit
     * @param time                   the time for median
     * @param percent20InsteadOfPlus use %20 instead of plus in url
     */
    @Deprecated
    public static void setSkinPrices(int limit, int time, boolean percent20InsteadOfPlus) {
        List<SkinFullName> skins = QRCS2Skin.getSkinsWithoutPrice(limit, false);
        for(SkinFullName skin: skins){
            setSkinPrice(skin, false, time, percent20InsteadOfPlus);
        }
    }

    /**
     * Sets skin prices for skins with special chars.
     *<p>switch between 'percent20InsteadOfPlus' to get a higher api request limit</p>
     * @param time                   the time for median
     * @param percent20InsteadOfPlus use %20 instead of plus in url
     */
    @Deprecated
    public static void setSkinPricesSpecialChars(int time, boolean percent20InsteadOfPlus) {
        List<SkinFullName> skins = QRCS2Skin.getSkinsWithoutPrice(Integer.MAX_VALUE, true);
        for(SkinFullName skin: skins){
            setSkinPrice(skin, true, time, percent20InsteadOfPlus);
        }
    }

    /**
     * Sets skin price/amountSold and saves to db.
     * <p>price/amount cases: success -> [price,amount], no data in time -> [price,-1], no data in 180 days [-2,-2] </p>
     *<p>switch between 'percent20InsteadOfPlus' to get a higher api request limit</p>
     * @param skin                   the skin as {@link SkinFullName}
     * @param withSpecialChars       true to set prices for skins with special chars
     * @param time                   the time for median
     * @param percent20InsteadOfPlus use %20 instead of plus in url
     */
    @Deprecated
    public static void setSkinPrice(SkinFullName skin, boolean withSpecialChars, int time, boolean percent20InsteadOfPlus) {
        double medianPrice = -1;
        int amountSold = 0;
        int backpackTime = 0;

        PriceType priceType = PriceType.getPriceType(skin.getCondition(), skin.getStattrak());
        String data;
        if(withSpecialChars){
            Map<String, String> specialCharsMapping = getSpecialSkinNamesMap(true);
            String newTitle = specialCharsMapping.get(skin.getTitle());
            data = CSGOBackpackApi.fetchSkinPriceData(skin.getFullNameSpecialChars(newTitle), time, percent20InsteadOfPlus);
        }else {
            data = CSGOBackpackApi.fetchSkinPriceData(skin.getFullName(), time, percent20InsteadOfPlus);
        }


        JSONObject jsonObject;
        try{

            jsonObject = new JSONObject(new JSONTokener(data));
        }catch (Exception ParseException){
            throw new RuntimeException("couldn't parse to JSONObject, data ->"+data+"\n for skin: "+skin.getFullName());
        }


        boolean success = Boolean.parseBoolean(String.valueOf(jsonObject.get("success")));
        if (!success) {
            log.debug("success: false for skin -> {}", skin.getFullName());
            String reason = (String) jsonObject.get("reason");
            if (reason != null && reason.contains("exceeded maximum number of requests")) {
                throw new RuntimeException("exceeded maximum number of requests");
            }else{
                //no price data for last 180 days
                int skinPriceId = QRSkinPrice.save(priceType, -2, -2);
                log.debug("No Data in 180 days, saving skin -> {} price: {}  {}", skin.getFullName(),-2,-2);
                QRCS2Skin.updatePrice(skin.getId(), skinPriceId);
                return;
            }
        }
        String medianPriceS = (String) jsonObject.get("median_price");
        String amountSoldS = (String) jsonObject.get("amount_sold");
        String bpTimeS = (String) jsonObject.get("time");
        if (medianPriceS != null && amountSoldS != null) {
            try {
                medianPrice = Double.parseDouble(medianPriceS);
                amountSold = Integer.parseInt(amountSoldS);
                backpackTime = Integer.parseInt(bpTimeS);
            } catch (NumberFormatException e) {
                log.warn("Couldn't convert price or amount from jsonObject: {}", jsonObject);
            }
        }else {
            log.error("Couldn't read price or amount from jsonObject:  {}", jsonObject);
        }

        if(time != backpackTime){
            amountSold=-1;
        }

        int skinPriceId = QRSkinPrice.save(priceType, medianPrice, amountSold);
        log.info("Saving SkinPrice -> {} price,amount: {}, {}", skin.getFullName(), medianPrice, amountSold );
        QRCS2Skin.updatePrice(skin.getId(), skinPriceId);
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

