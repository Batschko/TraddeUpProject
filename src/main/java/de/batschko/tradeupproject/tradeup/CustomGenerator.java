package de.batschko.tradeupproject.tradeup;

import de.batschko.tradeupproject.db.query.*;
import de.batschko.tradeupproject.enums.Condition;
import de.batschko.tradeupproject.enums.Rarity;
import de.batschko.tradeupproject.tables.records.StashSkinHolderRecord;
import de.batschko.tradeupproject.tables.records.TradeUpOutcomeRecord;
import de.batschko.tradeupproject.tables.records.TradeUpOutcomeSkinsRecord;
import de.batschko.tradeupproject.utils.ApiUtils;
import org.jooq.impl.UpdatableRecordImpl;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;


/**
 * Class to generate custom TradeUps.
 */
public class CustomGenerator {

    private static final double costMultiplier = 1.08;
    private static final double outSkinMultiplier = 0.9;

    /**
     * Calculate custom TradeUp-data from {@link JSONObject}.
     *
     * @param jsonObject jsonObject
     * @return TradeUp-data as JSONObject
     */
     public static JSONObject calculateTup(JSONObject jsonObject){
        return calculateTup(jsonObject, false);
    }

    /**
     * Calculate custom TradeUp-data from {@link JSONObject}.
     *
     * @param jsonObject jsonObject
     * @param save       save to database or not
     * @return TradeUp-data as JSONObject
     */
    public static JSONObject calculateTup(JSONObject jsonObject, boolean save){
        JSONObject row1 = jsonObject.getJSONObject("row1");
        JSONObject row2 = jsonObject.getJSONObject("row2");
        JSONObject row3 = jsonObject.getJSONObject("row3");
        Rarity rarity = Rarity.valueOf(jsonObject.getString("rarity"));
        Condition condTarget = Condition.valueOf(jsonObject.getString("condTarget"));
        boolean stat = jsonObject.getBoolean("stat");

        List<String> collectionList = new ArrayList<>(List.of(row1.getString("coll"),row2.getString("coll")));
        List<Integer> collNumber = new ArrayList<>(List.of(row1.getInt("count"),row2.getInt("count")));
        List<Condition> conditionList = new ArrayList<>(List.of(Condition.valueOf(row1.getString("cond")),Condition.valueOf(row2.getString("cond"))));
        if(!row3.isNull("coll")){
            collectionList.add(row3.getString("coll"));
            collNumber.add(row3.getInt("count"));
            conditionList.add(Condition.valueOf(row3.getString("cond")));
        }
        CollectionConditionDistribution collCondDistri = new CollectionConditionDistribution(collNumber, conditionList);
        TradeUpSettings settings = new TradeUpSettings(collectionList,collCondDistri,condTarget);

        int tupId=666666;
        QRCustomTradeUp.createTradeUpSkinsCustom(tupId,settings, rarity , stat? (byte) 1: 0);

        double totalPrice = 0;
        double floatSum = 0;
        double amountSoldSum = 0;
        //todo dynamic float dict
        Map<Condition,Double> floatDictMap = QRUtils.getFloatDictMap(2);
        List<StashSkinHolderRecord> possibleStashHolder = new ArrayList<>();

        for(int i=0; i<collectionList.size(); i++){
            String collectionName = collectionList.get(i);
            Condition condition = conditionList.get(i);
            if(collNumber.get(i) == 0){
                continue;
            }
            double minSkinPriceAvg = QRCS2Skin.getTradeUpSkinsAveragePrice(true ,collectionName, condition, tupId);

            totalPrice+= collNumber.get(i) * minSkinPriceAvg;
            floatSum+= collNumber.get(i) * floatDictMap.get(condition);
            possibleStashHolder.addAll(QRStashHolder.getByCollectionRarity(collectionName, Rarity.increase(rarity)));
        }
       // this.setFloatSumNeeded(floatSum);

        if(possibleStashHolder.isEmpty()) throw new RuntimeException("possibleStashHolder is empty");

        TradeUpOutcomeRecord tupOutcome = QRUtils.createRecordTradeUpOutcome();
        tupOutcome.setCustom((byte) 1);
        tupOutcome.setCost(totalPrice*costMultiplier);
        tupOutcome.setAmountSoldAvg(amountSoldSum);

       // return possibleStashHolder;

        Map<Integer, Set<TradeUpOutcomeSkinsRecord>> outcomeCS2SkinsMap = new HashMap<>();
        for(StashSkinHolderRecord stashHolder : possibleStashHolder){
            double x = stashHolder.getFloatEnd() - stashHolder.getFloatStart();
            double y = stashHolder.getFloatStart();
            double magic_float = (x * (floatSum / 10) + y);
            Condition resultingCondition =  Condition.getConditionByFloat(magic_float);
            int cs2SkinId = QRCS2Skin.getByStashHolderConditionStattrak(stashHolder.getStashId(), resultingCondition, stat ? (byte)1 : 0);

            TradeUpOutcomeSkinsRecord out_skin = QRUtils.createRecordTradeUpOutcomeSkins();
            out_skin.setCustom((byte) 1);
            out_skin.setCS2SkinId(cs2SkinId);
            out_skin.setTradeUpId(tupId);
            out_skin.setSkinFloat(magic_float);

            if(outcomeCS2SkinsMap.get(stashHolder.getCollectionId()) == null){
                outcomeCS2SkinsMap.put(stashHolder.getCollectionId(), new HashSet<>(Set.of(out_skin)));
            }else {
                outcomeCS2SkinsMap.get(stashHolder.getCollectionId()).add(out_skin);
            }
           // checkFloatMarker(magic_float);
        }

        double skinPool = 0;
        if(outcomeCS2SkinsMap.size()==1){
            //single collection
            skinPool = (10 * outcomeCS2SkinsMap.values().iterator().next().size());
        }else {
            for(Map.Entry<Integer, Set<TradeUpOutcomeSkinsRecord>> entry: outcomeCS2SkinsMap.entrySet()){
                String collName = QRCollection.getCollectionName(entry.getKey());
                int collIndex = settings.getCollectionListIndex(collName);
                skinPool+= (collNumber.get(collIndex) * entry.getValue().size());
            }
        }

        double hitChanceSum = 0;
        double skinAvgPrice =0;
        double skinMinPrice = Double.MAX_VALUE, skinMaxPrice = Double.MIN_VALUE;
        double categoryEven = 0, categoryProfit = 0;
        for(Map.Entry<Integer, Set<TradeUpOutcomeSkinsRecord>> entry: outcomeCS2SkinsMap.entrySet()){
            double chance;

            if(outcomeCS2SkinsMap.size()==1){
                //single collection
                chance = 10 / skinPool;
            }else {
                String collName = QRCollection.getCollectionName(entry.getKey());
                int collIndex = settings.getCollectionListIndex(collName);
                chance = collNumber.get(collIndex) / skinPool ;
            }
            for(TradeUpOutcomeSkinsRecord skin : entry.getValue()){
                skin.setChance(chance);

                double skinPrice = QRSkinPrice.getSkinPrice(skin.getCS2SkinId())*outSkinMultiplier;

                skinAvgPrice += skinPrice * chance;
                if(skinPrice > skinMaxPrice){
                    skinMaxPrice = skinPrice;
                }
                if(skinPrice < skinMinPrice){
                    skinMinPrice = skinPrice;
                }
                //categoryMarker
                if(skinPrice > tupOutcome.getCost() * 1.15){
                    categoryProfit += chance;
                } else if (skinPrice >= tupOutcome.getCost() * 0.85 && skinPrice <= tupOutcome.getCost() * 1.15) {
                    categoryEven += chance;
                }
                //hitChance
                if(skinPrice >= tupOutcome.getCost() * 0.85){
                    hitChanceSum += chance;
                }
            }

        }

        //categoryMarker
        if(categoryEven > categoryProfit * 2.5) tupOutcome.setCategoryMarker((byte) 1);
        tupOutcome.setOutcome((skinAvgPrice - tupOutcome.getCost()) / tupOutcome.getCost());
        tupOutcome.setChanceValue(skinAvgPrice - tupOutcome.getCost());
        tupOutcome.setLoss(tupOutcome.getCost() - skinMinPrice);
        tupOutcome.setHitChance(hitChanceSum);
        tupOutcome.setValue(skinMaxPrice - tupOutcome.getCost());
        tupOutcome.setRepeatFactor(tupOutcome.getValue() / tupOutcome.getLoss());
        tupOutcome.setRepeatFactorChance(skinAvgPrice / tupOutcome.getLoss());
        tupOutcome.setSkinAvg(skinAvgPrice);
        tupOutcome.setSkinMin(skinMinPrice);
        tupOutcome.setSkinMax(skinMaxPrice);


        for(Set<TradeUpOutcomeSkinsRecord> outSkins : outcomeCS2SkinsMap.values()){
            outSkins.forEach(UpdatableRecordImpl::store);
        }

        //TODO
        JSONArray tupskins = ApiUtils.skinResultToJsonArray(QRCS2Skin.getTradeUpSkinsCustom(tupId));
        JSONArray outskins = ApiUtils.skinResultToJsonArray(QRCS2Skin.getOutSkinsCustom(tupId));
        QRCS2Skin.deleteCustomInAndOutSkins(tupId);

        Map<String, Object> tupOutcomeMap = tupOutcome.intoMap();

        JSONObject tupOutcomeObject = new JSONObject();
        for (Map.Entry<String, Object> entry : tupOutcomeMap.entrySet()) {
            tupOutcomeObject.put(entry.getKey(), entry.getValue());
        }
        if(tupOutcomeObject.has("rarity"))tupOutcomeObject.put("rarity", tupOutcomeObject.getEnum(Rarity.class,"rarity"));


        JSONObject returnVal = new JSONObject();
        returnVal.put("tupskins",tupskins);
        returnVal.put("outskins",outskins);
        returnVal.put("outcome",tupOutcomeObject);

        System.out.println(returnVal);

        return returnVal;
    }

}
