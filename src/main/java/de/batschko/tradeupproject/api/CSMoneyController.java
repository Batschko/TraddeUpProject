package de.batschko.tradeupproject.api;

import de.batschko.tradeupproject.db.query.api.QRTradeUpTable;
import de.batschko.tradeupproject.enums.Condition;
import de.batschko.tradeupproject.utils.CSMoneyUtils;
import de.batschko.tradeupproject.webfetchers.CSMoneyWiki;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.util.StopWatch;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@RestController
public class CSMoneyController {


    @GetMapping(value = "/api/csmoney/{tupId}", produces = "application/json" )
    public String csmoneyUrl(@PathVariable int tupId) {
        log.info("sending CSMoney url");
        return CSMoneyUtils.getCSMoneyUrlFiltered(tupId);
    }

    @GetMapping(value = "/api/csmoney/bot/{tupId}", produces = "application/json" )
    public Map<String, List<Double>> csmoneyBotItems(@PathVariable int tupId) {
        log.info("sending CSMoney bot items");
        Map<String, Map<String, List<Double>>> data = CSMoneyWiki.fetchBotItems(List.of(tupId));
        return data.entrySet().iterator().next().getValue();
    }

    @GetMapping(value = "/api/csmoney/bot/active", produces = "application/json" )
    public String csmoneyWatchedBotItemsAmountREal() {
        log.info("sending CSMoney bot active");
        StopWatch watch = new StopWatch();
        watch.start();
        List<Integer> ids = QRTradeUpTable.getTradeUpsActiveIds();
        Map<String, Map<String, List<Double>>> botItems = CSMoneyWiki.fetchBotItems(ids);

        JSONArray jsonArray = new JSONArray();
        String regex = ".*&collection=([^&]*)&isStatTrak=([^&]*)&limit=.*&rarity=([^&]*)&sort";
        Pattern pattern = Pattern.compile(regex);
        int amountAll = 0;
        int goodAmountAll = 0;
        int id = 0;
        StringBuilder urlSb = new StringBuilder();
        for (Map.Entry<String, Map<String, List<Double>>> entry : botItems.entrySet()) {
            urlSb.setLength(0);
            urlSb.append("https://cs.money/csgo/trade/?");
            JSONObject jsonObject = new JSONObject();
            int amountTup = 0;
            int goodAmount = 0;
            for (Map.Entry<String, List<Double>> a : entry.getValue().entrySet()){
                amountTup+=a.getValue().size();

                for(double floats: a.getValue()){
                    if(a.getKey().contains(Condition.FN.getText())){
                        if(floats < 0.024)  jsonObject.put("floatGood", ++goodAmount);
                    }else if(a.getKey().contains(Condition.MW.getText())){
                        if(floats < 0.084)  jsonObject.put("floatGood", ++goodAmount);
                    }else if(a.getKey().contains(Condition.FT.getText())){
                        if(floats < 0.188)  jsonObject.put("floatGood", ++goodAmount);
                    }

                }

            }
            amountAll+=amountTup;
            goodAmountAll+=goodAmount;

            jsonObject.put("amount", amountTup);
            jsonObject.put("id", ++id);


            if(entry.getKey().startsWith("https")){
                Matcher matcher = pattern.matcher(entry.getKey());
                matcher.find();
                //entry key collRarityStat = matcher.group(1) + " - " + matcher.group(2) + " - " +matcher.group(3);
                String collection = matcher.group(1);
                String stat = matcher.group(2);
                String rarity = matcher.group(3);
                jsonObject.put("collection", collection);
                jsonObject.put("rarity", rarity);
                jsonObject.put("stat", stat);
                jsonObject.put("data", entry.getValue());

                urlSb.append("rarity=").append(rarity);
                urlSb.append("&isStatTrak=").append(stat);
                urlSb.append("&collection=").append(collection);
                jsonObject.put("url", urlSb.toString());

            }else {
                //TODO
                new RuntimeException();
            }


            jsonArray.put(jsonObject);
        }
        jsonArray.put(new JSONObject(Map.of("amountAll", amountAll, "goodAmountAll", goodAmountAll)));
        return jsonArray.toString();
    }
}
