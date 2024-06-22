package de.batschko.tradeupproject.webfetchers;

import de.batschko.tradeupproject.db.customtable.TradeUpCustom;
import de.batschko.tradeupproject.db.query.QRTradeUpGenerated;
import de.batschko.tradeupproject.enums.PriceType;
import de.batschko.tradeupproject.tables.CS2Skin;
import de.batschko.tradeupproject.utils.SkinUtils;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * Class to fetch from CSGOBackpack api.
 */
@Slf4j
@Deprecated
public class CSGOBackpackApi {

    /**
     * Fetch skin price data.
     * <p>switch between 'percent20InsteadOfPlus' to get a higher api request limit</p>
     * @param fullItemName           the full skin name
     * @param time                   the api time parameter
     * @param percent20InsteadOfPlus use %20 instead of plus in url
     * @return json data from skin price api
     */
    @Deprecated
    public static String fetchSkinPriceData(String fullItemName, int time, boolean percent20InsteadOfPlus)
    {
        StringBuilder content = new StringBuilder();
        String encodedItemId = URLEncoder.encode(fullItemName, StandardCharsets.UTF_8);
        String urlString = "http://csgobackpack.net/api/GetItemPrice/?currency=" + "EUR" + "&id=" + encodedItemId + "&time=" + time + "&extend=1";
        if(percent20InsteadOfPlus){
            urlString = urlString.replace("+","%20");
        }
        log.debug("Fetching content from: {}", urlString);
        try
        {
            URL url = new URI(urlString).toURL();
            URLConnection urlConnection = url.openConnection();

            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
            String line;
            while ((line = bufferedReader.readLine()) != null)
            {
                content.append(line);
            }
            bufferedReader.close();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        return content.toString();
    }

    /**
     * Calculates all trade ups using backpack price.
     * <p>Gets all TradeUps which are not calculated from db and calculates them</p>
     */
    @Deprecated
    public static void calculateAllTradeUpsBackpackPrice(){
        List<TradeUpCustom> tradeUps = QRTradeUpGenerated.getTradeUpsToCalculate();
        for(TradeUpCustom tup : tradeUps){
            tup.setCalculation(false);
        }
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
        List<SkinUtils.SkinFullName> skins = getSkinsWithoutPrice(limit, false);
        for(SkinUtils.SkinFullName skin: skins){
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
        List<SkinUtils.SkinFullName> skins = getSkinsWithoutPrice(Integer.MAX_VALUE, true);
        for(SkinUtils.SkinFullName skin: skins){
            setSkinPrice(skin, true, time, percent20InsteadOfPlus);
        }
    }

    /**
     * Sets skin price/amountSold and saves to db.
     * <p>price/amount cases: success -> [price,amount], no data in time -> [price,-1], no data in 180 days [-2,-2] </p>
     *<p>switch between 'percent20InsteadOfPlus' to get a higher api request limit</p>
     * @param skin                   the skin as {@link SkinUtils.SkinFullName}
     * @param withSpecialChars       true to set prices for skins with special chars
     * @param time                   the time for median
     * @param percent20InsteadOfPlus use %20 instead of plus in url
     */
    @Deprecated
    public static void setSkinPrice(SkinUtils.SkinFullName skin, boolean withSpecialChars, int time, boolean percent20InsteadOfPlus) {
        double medianPrice = -1;
        int amountSold = 0;
        int backpackTime = 0;

        PriceType priceType = PriceType.getPriceType(skin.getCondition(), skin.getStattrak());
        String data;
        if(withSpecialChars){
            Map<String, String> specialCharsMapping = SkinUtils.getSpecialSkinNamesMap(true);
            String newTitle = specialCharsMapping.get(skin.getTitle());
            data = fetchSkinPriceData(skin.getFullNameSpecialChars(newTitle), time, percent20InsteadOfPlus);
        }else {
            data = fetchSkinPriceData(skin.getFullName(), time, percent20InsteadOfPlus);
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
            /*    int skinPriceId = QRSkinPrice.save(priceType, -2, -2);
                log.debug("No Data in 180 days, saving skin -> {} price: {}  {}", skin.getFullName(),-2,-2);
                QRCS2Skin.updatePrice(skin.getId(), skinPriceId);*/
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

        /*  int skinPriceId = QRSkinPrice.save(priceType, medianPrice, amountSold);
        log.info("Saving SkinPrice -> {} price,amount: {}, {}", skin.getFullName(), medianPrice, amountSold );
        QRCS2Skin.updatePrice(skin.getId(), skinPriceId);*/
    }

    /**
     * Gets {@link CS2Skin}s without price as {@link SkinUtils.SkinFullName}.
     *
     * @param limit       limit
     * @param specialChar true -> use special chars
     * @return list of {@link SkinUtils.SkinFullName}
     */
    public static List<SkinUtils.SkinFullName> getSkinsWithoutPrice(int limit, boolean specialChar) {
      /*  List<String> specialChars = new ArrayList<>(SkinUtils.getSpecialSkinNamesMap().values());
        org.jooq.Condition condSpecialChars;
        if(specialChar){
            condSpecialChars = V_FULLCS2SKIN.TITLE.in(specialChars);
        }else {
            condSpecialChars = V_FULLCS2SKIN.TITLE.notIn(specialChars);
        }

        Result<Record6<Integer, Byte, String, String, Condition, Double>> result = QueryRepository.dsl.select(
                        V_FULLCS2SKIN.ID, V_FULLCS2SKIN.STATTRAK, V_FULLCS2SKIN.WEAPON,
                        V_FULLCS2SKIN.TITLE, V_FULLCS2SKIN.CONDITION, V_FULLCS2SKIN.PRICE)
                .from(V_FULLCS2SKIN)
                .where(condSpecialChars.and(V_FULLCS2SKIN.SKIN_PRICE_ID.isNull()))
                .limit(limit)
                .fetch();
        List<SkinUtils.SkinFullName> resultList = new ArrayList<>();
        result.forEach(record -> {
            SkinUtils.SkinFullName test = new SkinUtils.SkinFullName(
                    record.get(0, Integer.class),
                    record.get(1, Byte.class),
                    record.get(2, String.class),
                    record.get(3, String.class),
                    record.get(4, Condition.class));
            resultList.add(test);
        });
        return resultList;*/
        return null;
    }
}
