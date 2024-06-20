package de.batschko.tradeupproject.webfetchers;

import de.batschko.tradeupproject.db.query.QRCS2Skin;
import de.batschko.tradeupproject.db.query.QRCSMoney;
import de.batschko.tradeupproject.db.query.QRCSMoneyPrice;
import de.batschko.tradeupproject.enums.Condition;
import de.batschko.tradeupproject.enums.Rarity;
import lombok.extern.slf4j.Slf4j;
import org.jooq.Record;
import org.jooq.Record4;
import org.jooq.Record6;
import org.jooq.Result;
import org.json.JSONArray;
import org.json.JSONObject;
//import org.json.parser.JSONParser;
//import org.json.simple.parser.ParseException;
import org.json.JSONTokener;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.*;

import static de.batschko.tradeupproject.tables.VFullcs2skin.V_FULLCS2SKIN;

/**
 * Jsoup Scraper for CSMoneyWiki data
 */
@Slf4j
public class CSMoneyWiki {


    public static void fetchPrices(boolean update, int skip) throws IOException {
        //Result<Record2<String, String>> a = QRCSMoneyPrice.getCSMoneyPriceList();
        Result<Record4<String, String, Double, Double>> skinNameWithFloatList;
        if(update){
          //  a = QRCSMoneyPrice.getCSMoneyPriceListNegativ();
            skinNameWithFloatList = QRCSMoneyPrice.getCSMoneyPriceListByDate();
        }else {
            skinNameWithFloatList = QRCSMoneyPrice.getCSMoneyPriceList();

        }
        for(int i=0; i<skip; i++){
            skinNameWithFloatList.removeFirst();
        }


        for(Record4<String, String, Double, Double> skinNameWithFloat : skinNameWithFloatList){

            String weapon = skinNameWithFloat.get(0, String.class);
            String title = skinNameWithFloat.get(1, String.class);
            if(title.contains("rmungandr")){
                continue;
            }
            if(title.endsWith("lnir") && title.startsWith("Mj")){
                continue;
            }
            if (title.equals("Dragon King")) {
                title = "龍王 (Dragon King)";
            }
            double floatStart = skinNameWithFloat.get(2, Double.class);
            double floatEnd = skinNameWithFloat.get(3, Double.class);
            fetchWikiPrice(weapon, title, floatStart, floatEnd, update);
            Random random = new Random();
            int minTime = 1300; // Minimum time in milliseconds1300
            int maxTime = 2200; // Maximum time in milliseconds2000

            try {
                int sleepTime = random.nextInt(maxTime - minTime + 1) + minTime;
                Thread.sleep(sleepTime);
                log.info("Slept for " + sleepTime + " milliseconds.");
            } catch (InterruptedException e) {
                log.error(e.getMessage());
            }
        }




    }


    public static void fetchWikiPrice(String weapon, String title, double floatStart, double floatEnd, boolean update) throws IOException {
        log.info(weapon+title);
        String url = "https://wiki.cs.money/api/graphql";
        String skin_name = weapon +" | "+title;
        String requestBody = "{\"operationName\":\"get_min_available\",\"variables\":{\"name\":\""+skin_name+"\"},\"query\":\"query get_min_available($name: String!) {\\n  get_min_available(name: $name) {\\n    name\\n    isSouvenir\\n    isStatTrack\\n    bestPrice\\n    bestSource\\n    source {\\n      trade {\\n        lowestPrice\\n        count\\n      }\\n      market {\\n        lowestPrice\\n        count\\n      }\\n    }\\n  }\\n}\"}";

        Connection.Response response = Jsoup.connect(url)
                .header("Content-Type", "application/json")
                .header("Content-Length", Integer.toString(requestBody.length()))
                .header("Referer", "https://wiki.cs.money/weapons/"+weapon+"/"+title)
                .header("Origin", "https://wiki.cs.money")
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/100.0.4896.100 Safari/537.36")
                .requestBody(requestBody)
                .method(Connection.Method.POST)
                .ignoreContentType(true)
                .execute();

        Document document = response.parse();

        String test = document.childNode(0).childNode(1).childNode(0).toString();


        JSONObject jsonObject;
        try{
            jsonObject = new JSONObject(new JSONTokener(test));
            JSONArray array = jsonObject.getJSONObject("data").getJSONArray("get_min_available");

            if(array == null){
                log.warn("skip"+weapon+" "+title);
                return;
            }
            //TODO
            //Csmoney beim return fehlen conditions um im verlauf doppelte abfragen zu vermeiden, direkt fehlende conds mit -1 speichern
            List<Condition> unsavedConds = Condition.getPossibleConditions(floatStart,floatEnd);
            List<Condition> unsavedCondsStat = new ArrayList<>(unsavedConds);
            int possibleCondCount = unsavedConds.size();
            for (int i=0; i<array.length(); i++) {
                //no souvenirs!
                JSONObject element = array.getJSONObject(i);
                if((boolean) element.get("isSouvenir")) break;
                String name = element.get("name").toString();
                Condition condition = null;
                for(Condition cond : Condition.values()){
                    if (name.contains(cond.getText())){
                        if(name.contains("StatTrak")){
                            unsavedCondsStat.remove(cond);
                        }else {
                            unsavedConds.remove(cond);
                        }
                        condition = cond;
                        break;
                    }
                }

                byte stat = (boolean) element.get("isStatTrack") ? (byte) 1 : (byte) 0;
                JSONObject priceObj = element.getJSONObject("source").getJSONObject("trade");
                String price,count;
                try{
                    if (priceObj != null && priceObj.has("lowestPrice") && priceObj.has("count")) {
                        price = priceObj.get("lowestPrice").toString();
                        count = priceObj.get("count").toString();
                        //QRCSMoneyPrice.update(weapon, title, condition, stat, Double.parseDouble(price), Integer.parseInt(count));
                        QRCSMoneyPrice.update(weapon, title, condition, stat, Double.parseDouble(price));

                    } else {
                        if(!update){

                           // QRCSMoneyPrice.save(weapon, title, condition, stat, -1, -1);
                        }
                    }
                }catch (Exception e){
                    System.out.println(weapon+title);
                }

            }
            //TODO bei
            //ADD missing cconditions

            //TODO stuff below?
            if(update){
                return;
            }

            /* not needed anymore?
            for(Condition cond : unsavedConds){
                    //QRCSMoneyPrice.update(weapon, title, cond, (byte) 0, -1);
                    QRCSMoneyPrice.update(weapon, title, cond, (byte) 0, (Double) null);
            }
            if(unsavedCondsStat.size() != possibleCondCount){
                for(Condition cond : unsavedCondsStat){
                    //QRCSMoneyPrice.update(weapon, title, cond, (byte) 1, -1);
                    QRCSMoneyPrice.update(weapon, title, cond, (byte) 1, (Double) null);
                }
            }*/

        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }



    public static void updateCSMoneySkinPrice(int skinId){
        Record skin = QRCS2Skin.getFullSkin(skinId);
        String fullName = skin.get(V_FULLCS2SKIN.WEAPON)+" "+skin.get(V_FULLCS2SKIN.TITLE)+" ("+skin.get(V_FULLCS2SKIN.CONDITION).getText()+")";
        boolean stattrak = skin.get(V_FULLCS2SKIN.STATTRAK)==1;
        String url = "https://cs.money/5.0/load_bots_inventory/730?isStatTrak="+stattrak+"&limit=60&name="+fullName+"&offset=0&order=asc&priceWithBonus=0&sort=price&withStack=true";
        String body = getCSMoneyDocumentBody(url);


        JSONObject jsonObject;
        JSONArray array;
        try{

            jsonObject = new JSONObject(new JSONTokener(body));
            if(!jsonObject.has("items")){
                log.warn("no skin found {} : {}", fullName, url);
                return;
            }
            array = jsonObject.getJSONArray("items");


            JSONObject firstJsonObject =  array.getJSONObject(0);
            double price = Double.parseDouble(String.valueOf(firstJsonObject.get("defaultPrice")));
            QRCSMoneyPrice.update(skinId, price);
            log.info("updated {} {} : {}", fullName, stattrak, price);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Map<String, Map<String, List<Double>>> fetchBotItems(List<Integer> tupIds){
        //      url,     skinName,   condition, float
        HashMap<String, Map<String, Map<String, Double>>> urlSkinMap = getCSMoneyUrls(tupIds);
        boolean fetchedAll;
        int offsetVal;
        //  url      skinName    floats
        Map<String, Map<String, List<Double>>> skinsOnBot = new TreeMap<>();
        for(String url : urlSkinMap.keySet()){
            fetchedAll = false;
            offsetVal = 0;
            //skinName, available floats
            Map<String, List<Double>> urlSkinsOnBot = new HashMap<>();
            while(!fetchedAll){
                String urlOffset = url.replaceAll("(?<=&offset=)\\d+", String.valueOf(offsetVal));
                log.debug("fetching {}",urlOffset);
                try {
                    Thread.sleep(1500);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                String body = getCSMoneyDocumentBody(urlOffset);


                JSONObject jsonObject;
                JSONArray array;
                try{
                    jsonObject = new JSONObject(new JSONTokener(body));
                    if(!jsonObject.has("items")){
                        log.warn("aaaaaaaaaaa\naaaaaaa\naaa\naaaa\naaaa\naaaaaaaa\naaaaa\naaaaaa\njson array is null");
                        break;
                    }
                    array = jsonObject.getJSONArray("items");

                    if(array.length()==60){
                        offsetVal+=60;
                    }else {
                        fetchedAll = true;
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

                //csmoney bot data
                for (int i=0; i<array.length(); i++) {
                    JSONObject element = array.getJSONObject(i);

                    if(element.get("overpay").getClass()!=JSONObject.NULL.getClass()) continue;
                    String fullName = (String)element.get("fullName");
                    Map<String, Map<String, Double>> skinmap = urlSkinMap.get(url);
                    if(skinmap.containsKey(fullName)){
                        if(skinmap.get(fullName).containsKey((String) element.get("quality"))){
                            double maxFloat = skinmap.get(fullName).get((String) element.get("quality"));
                            double skinFloat = Double.parseDouble((String) element.get("float"));
                            if(skinFloat <= maxFloat){
                                if(urlSkinsOnBot.containsKey(fullName)) urlSkinsOnBot.get(fullName).add(skinFloat);
                                else urlSkinsOnBot.put(fullName, new ArrayList<>(List.of(skinFloat)));
                            }
                        }
                    }
                }//loop: array fetched skins
            }//loop: fetchedAll
            for(List<Double> values: urlSkinsOnBot.values()){
                Collections.sort(values);
            }
            skinsOnBot.put(url, urlSkinsOnBot);

        }//loop url
        return skinsOnBot;
    }


    public static String getCSMoneyDocumentBody(String url){
        Connection.Response response;
        try {
            response = Jsoup.connect(url)
                    .method(Connection.Method.GET)
                    .ignoreContentType(true)
                    .execute();
        } catch (IOException e) {
            //TODO maybe remove
            try {

                log.warn("error 249 waiting 30sec");
                Thread.sleep(30000);
                response = Jsoup.connect(url)
                        .method(Connection.Method.GET)
                        .ignoreContentType(true)
                        .execute();
            } catch (IOException | InterruptedException ex) {
                throw new RuntimeException(ex);
            }
        }
        Document document;
        try {
            document = response.parse();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return document.childNode(0).childNode(1).childNode(0).toString();
    }

    public static HashMap<String, Map<String, Map<String, Double>>> getCSMoneyUrls(List<Integer> tupIds) {
        HashMap<String, Map<String, Map<String, Double>>> urlSkinMap = new HashMap<>();
        for (int id : tupIds) {
            Result<Record6<String, String, String, Rarity, Byte, Condition>> tupSkinInfo = QRCSMoney.getTradeUpSkinInfo(id);
            String collName = tupSkinInfo.getFirst().value3();
            if (collName.contains("Case")) {
                collName = collName.replace("Case", "Collection");
            } else {
                collName += " Collection";
            }
            String rarity = tupSkinInfo.getFirst().value4().getText();
            boolean stattrak = tupSkinInfo.getFirst().value5() == 1;
            Map<String, Double> condMap = new HashMap<>();
            Map<String, Map<String, Double>> skinmap = new HashMap<>();
            double skinFloat;
            for (Record6<String, String, String, Rarity, Byte, Condition> info : tupSkinInfo) {
                Condition cond = info.value6();
                switch (cond) {
                    case FN -> {
                        skinFloat = 0.041;
                    }
                    case MW -> {
                        skinFloat = 0.105;
                    }
                    case FT -> {
                        skinFloat = 0.21;
                    }
                    case WW -> {
                        skinFloat = 0.45;
                    }
                    default -> throw new RuntimeException("getCSMoneyUrls() got unexpected condition: "+cond);
                }
                if (stattrak) {
                    skinmap.put("StatTrak™ " + info.value1() + " | " + info.value2() + " (" + cond.getText() + ")", new HashMap<>(Map.of(cond.toString().toLowerCase(), skinFloat)));
                } else {
                    skinmap.put(info.value1() + " | " + info.value2() + " (" + cond.getText() + ")", new HashMap<>(Map.of(cond.toString().toLowerCase(), skinFloat)));
                }

                condMap.put(cond.toString().toLowerCase(), skinFloat);
            }
            urlSkinMap.put(createCSMoneyUrl(collName, rarity, stattrak, condMap), skinmap);
        }

       return urlSkinMap;
    }

    public static String createCSMoneyUrl(String collName, String rarityVal, boolean stattrak, Map<String, Double> condMap){
        String baseUrl = "https://cs.money/5.0/load_bots_inventory/730?";
        String souvenir = "isSouvenir=false";
        String stat = "&isStatTrak=" + stattrak;
        String limit = "&limit=60";
        String order = "&order=desc&priceWithBonus=0";
        String rarity = "&rarity=" + rarityVal;
        int defaultOffset = 0;
        //TODO (if it gets more map to replace)
        if (collName.contains("Dust 2 (Old)")) collName = "Dust 2 Collection";
        if (collName.contains("Nuke (2018)")) collName = "2018 Nuke Collection";
        String collection = "&collection=The " + collName;

        StringBuilder sb = new StringBuilder();
        sb.append(baseUrl).append(souvenir).append(collection).append(stat)
                .append(limit).append("&offset=").append(defaultOffset)
                .append(order);

        for(String cond: condMap.keySet()){
            sb.append("&quality=").append(cond);
        }
        sb.append(rarity).append("&sort=price&withStack=true");
        return sb.toString();
    }



}


