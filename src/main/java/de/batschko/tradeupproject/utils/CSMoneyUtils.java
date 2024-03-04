package de.batschko.tradeupproject.utils;

import de.batschko.tradeupproject.db.query.QRUtils;
import de.batschko.tradeupproject.enums.Rarity;
import de.batschko.tradeupproject.webfetchers.CSMoneyScraper;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

/**
 * Utility methods for CSMoney
 */
public class CSMoneyUtils {

    /**
     * Gets cs money url filtered for TradeUp-Skins.
     *
     * @param tupId the TradeUp id
     */
    public static void getCSMoneyUrlFiltered(int tupId) {
        List<SkinUtils.TradeUpSkinInfo> skinList = QRUtils.getTradeUpSkinsInfo(tupId);
        StringBuilder sb = new StringBuilder();
        String baseUrl = ("https://cs.money/csgo/trade/");
        for (SkinUtils.TradeUpSkinInfo skin : skinList) {
            sb.setLength(0);
            Rarity rarity = skin.getRarity();
            byte stat = skin.getStattrak();
            String coll = skin.getColl_name();
            coll = coll.replace("Case", "Collection");
            sb.append("?sort=float&order=asc");
            sb.append("&rarity=").append(rarity.getText());
            sb.append("&isStatTrak=").append(stat == 1);
            sb.append("&collection=").append("The ").append(coll);

            String encoded = sb.toString().replace(" ", "+");

            try {
                System.out.println(new URI(baseUrl + encoded).toURL() + "\t\t\t" + skin.getStattrak() + " " + skin.getCondition());
            } catch (MalformedURLException | URISyntaxException e) {
                throw new RuntimeException(e);
            }
        }
    }


    /**
     * CSMoney price check fur TradeUp.
     * <p>asks for coll number input in the process</p>
     * @param tupId the TradeUp id
     */
    public static void csMoneyPriceCheck(int tupId) {
        List<SkinUtils.SkinFullName> outcomeSkins = QRUtils.getTradeUpOutcomeSkins(tupId);
        List<Map<String, Double>> outcomeSkinPrices = CSMoneyScraper.getCSMoneyWikiPriceMapList(outcomeSkins);
        Map<String, Double> outConditions = outcomeSkinPrices.removeFirst();
        printInputOutputSkins(false, outConditions, outcomeSkinPrices);

        List<SkinUtils.SkinFullName> tradeUpSkins = QRUtils.getTradeUpSkins(tupId);
        List<Map<String, Double>> tradUpSkinPrices = CSMoneyScraper.getCSMoneyWikiPriceMapList(tradeUpSkins);
        Map<String, Double> tradUpConditions = tradUpSkinPrices.removeFirst();
        Map<String, List<Double>> tradeUpMap = printInputOutputSkins(true, tradUpConditions, tradUpSkinPrices);

        Scanner in = new Scanner(System.in);
        double price = 0;
        System.out.println("Enter condition amount");
        for (Map.Entry<String, List<Double>> entry : tradeUpMap.entrySet()) {
            System.out.print(entry.getKey() + ": ");
            double amount = in.nextDouble();
            double cPrice = entry.getValue().getFirst() * amount;
            System.out.println(entry.getKey() + ": " + cPrice + " = " + entry.getValue().getFirst() + " * " + amount);
            price += cPrice;
        }

        System.out.println(">>>price: " + price + "  /  "+price*1.1);
        printInputOutputSkins(false, outConditions, outcomeSkinPrices);
       }


    private static Map<String, List<Double>> printInputOutputSkins(boolean input, Map<String, Double> skinConditions, List<Map<String, Double>> skinPrices){
        Map<String,List<Double>> resultMap = new HashMap<>();
        for(Map<String, Double> outMap : skinPrices){
            for(Map.Entry<String, Double> conds : skinConditions.entrySet()){
                if(conds.getKey()!=null){
                    if(resultMap.get(conds.getKey())==null){
                        resultMap.put(conds.getKey(), new ArrayList<>(List.of(outMap.get(conds.getKey()))));
                    }else {
                        resultMap.get(conds.getKey()).add(outMap.get(conds.getKey()));
                    }
                }
            }
        }
        String values;
        if(input){
            values = "InputSkins";
            System.out.println( String.format("\\--->>%s<<%s\\", values, "-".repeat(100 - 10 - values.length())));
        }else {
            values = "OutSkins";
            System.out.println( String.format("\\--->>%s<<%s\\", values, "-".repeat(100 - 9 - values.length())));
        }

        for(Map.Entry<String, List<Double>> entry : resultMap.entrySet()){
            try {
                entry.getValue().sort(Double::compareTo);
                values = entry.getKey()+":  "+entry.getValue();
                System.out.println( String.format("\\---%s%s\\", values, "-".repeat(100 - 6 - values.length())));
            }catch (Exception ignored){
            }

        }
        values = "";
        System.out.println( String.format("\\---%s%s\\", values, "-".repeat(100 - 6 - values.length())));
        return resultMap;
    }
}
