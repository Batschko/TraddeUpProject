package de.batschko.tradeupproject.db.customtable;

import de.batschko.tradeupproject.db.query.*;
import de.batschko.tradeupproject.enums.Condition;
import de.batschko.tradeupproject.enums.Rarity;
import de.batschko.tradeupproject.enums.TradeUpStatus;
import de.batschko.tradeupproject.tables.records.StashSkinHolderRecord;
import de.batschko.tradeupproject.tables.records.TradeUpOutcomeRecord;
import de.batschko.tradeupproject.tables.records.TradeUpOutcomeSkinsRecord;
import de.batschko.tradeupproject.tables.records.TradeUpRecord;
import de.batschko.tradeupproject.tradeup.TradeUpSettings;
import lombok.extern.slf4j.Slf4j;
import org.jooq.impl.UpdatableRecordImpl;

import java.util.*;

/**
 * Class to extend {@link TradeUpRecord}.
 *
 */
@Slf4j
public class TradeUpCustom extends TradeUpRecord {

    //used for calculation
    private TradeUpOutcomeRecord tupOutcome;
    private static final double costMultiplier = 1.08;
    private static final double outSkinMultiplier = 0.9;


    private List<StashSkinHolderRecord> calcStep1PossibleStashHolder(TradeUpSettings tradeUpSettings){
        double totalPrice = 0;
        double floatSum = 0;
        List<String> collList = tradeUpSettings.getCollectionList();
        List<Condition> condList = tradeUpSettings.getConditionList();
        List<Integer> collNumber = tradeUpSettings.getCollNumber();
        Map<Condition,Double> floatDictMap = QRUtils.getFloatDictMap(this.getFloatDictId());
        List<StashSkinHolderRecord> possibleStashHolder = new ArrayList<>();


        for(int i=0; i<collList.size(); i++){
            String collectionName = collList.get(i);
            Condition condition = condList.get(i);
            if(collNumber.get(i) == 0){
                continue;
            }
            double minSkinPriceAvg = QRCS2Skin.getTradeUpSkinsAveragePrice(false, collectionName, condition, this.getId());

            if(minSkinPriceAvg <= 0){
                this.setStatus(TradeUpStatus.WASTED);
                this.store();
                return null;
            }

            totalPrice+= collNumber.get(i) * minSkinPriceAvg;
            floatSum+= collNumber.get(i) * floatDictMap.get(condition);
            possibleStashHolder.addAll(QRStashHolder.getByCollectionRarity(collectionName, Rarity.increase(this.getRarity())));
        }
        this.setFloatSumNeeded(floatSum);

        if(possibleStashHolder.isEmpty()) throw new RuntimeException("possibleStashHolder is empty");

        this.tupOutcome = QRUtils.createRecordTradeUpOutcome();
        this.tupOutcome.setCustom((byte) 0);
        this.tupOutcome.setTradeupId(this.getId());
        this.tupOutcome.setCost(totalPrice*costMultiplier);


        return possibleStashHolder;
    }

    private Map<Integer, Set<TradeUpOutcomeSkinsRecord>> calcStep2outcomeSkins(List<StashSkinHolderRecord> possibleStashHolder){
        Map<Integer, Set<TradeUpOutcomeSkinsRecord>> outcomeCS2SkinsMap = new HashMap<>();
        for(StashSkinHolderRecord stashHolder : possibleStashHolder){
            double x = stashHolder.getFloatEnd() - stashHolder.getFloatStart();
            double y = stashHolder.getFloatStart();
            double magic_float = (x * (this.getFloatSumNeeded() / 10) + y);
            Condition resultingCondition =  Condition.getConditionByFloat(magic_float);
            int cs2SkinId = QRCS2Skin.getByStashHolderConditionStattrak(stashHolder.getStashId(), resultingCondition, this.getStattrak());
            TradeUpOutcomeSkinsRecord out_skin = QRUtils.createRecordTradeUpOutcomeSkins();
            out_skin.setCustom((byte) 0);
            out_skin.setCS2SkinId(cs2SkinId);
            out_skin.setTradeUpId(this.getId());
            out_skin.setSkinFloat(magic_float);

            if(outcomeCS2SkinsMap.get(stashHolder.getCollectionId()) == null){
                outcomeCS2SkinsMap.put(stashHolder.getCollectionId(), new HashSet<>(Set.of(out_skin)));
            }else {
                outcomeCS2SkinsMap.get(stashHolder.getCollectionId()).add(out_skin);
            }

            checkFloatMarker(magic_float);
        }
        return outcomeCS2SkinsMap;
    }

    /**
     * Sets calculation.
     * <p>also creates {@link TradeUpOutcomeSkinsRecord}s and {@link TradeUpOutcomeRecord} </p>
     *
     */
    public void setCalculation(boolean csMoneyPrice) {
        //Calc step 1
        TradeUpSettings tradeUpSettings = QRGenerationSettings.getTradeUpSettings(this.getGenerationSettingsId());
        List<StashSkinHolderRecord> possibleStashHolder = calcStep1PossibleStashHolder(tradeUpSettings);
        if(possibleStashHolder == null) return;
        //Calc step 2
        Map<Integer, Set<TradeUpOutcomeSkinsRecord>> outSkinsMap = calcStep2outcomeSkins(possibleStashHolder);
        //Calc step 3
        List<Integer> collNumber = tradeUpSettings.getCollNumber();
        double skinPool = 0;
        if(outSkinsMap.size()==1){
            //single collection
            skinPool = (10 * outSkinsMap.values().iterator().next().size());
        }else {
            for(Map.Entry<Integer, Set<TradeUpOutcomeSkinsRecord>> entry: outSkinsMap.entrySet()){
                String collName = QRCollection.getCollectionName(entry.getKey());
                int collIndex = tradeUpSettings.getCollectionListIndex(collName);
                skinPool+= (collNumber.get(collIndex) * entry.getValue().size());
            }
        }

        double hitChanceSum = 0;
        double skinAvgPrice = 0;
        double skinMinPrice = Double.MAX_VALUE, skinMaxPrice = Double.MIN_VALUE;
        double categoryEven = 0, categoryProfit = 0;
        for(Map.Entry<Integer, Set<TradeUpOutcomeSkinsRecord>> entry: outSkinsMap.entrySet()){
            double chance;

            if(outSkinsMap.size()==1){
                //single collection
                chance = 10 / skinPool;
            }else {
                String collName = QRCollection.getCollectionName(entry.getKey());
                int collIndex = tradeUpSettings.getCollectionListIndex(collName);
                chance = collNumber.get(collIndex) / skinPool ;
            }
            for(TradeUpOutcomeSkinsRecord skin : entry.getValue()){
                skin.setChance(chance);

                double skinPrice = QRSkinPrice.getSkinPrice(skin.getCS2SkinId())* outSkinMultiplier;

                skinAvgPrice += skinPrice * chance;
                if(skinPrice > skinMaxPrice){
                    skinMaxPrice = skinPrice;
                }
                if(skinPrice < skinMinPrice){
                    skinMinPrice = skinPrice;
                }
                //categoryMarker
                if(skinPrice > this.tupOutcome.getCost() * 1.15){
                    categoryProfit += chance;
                } else if (skinPrice >= this.tupOutcome.getCost() * 0.85 && skinPrice <= this.tupOutcome.getCost() * 1.15) {
                    categoryEven += chance;
                }
                //hitChance
                if(skinPrice >= this.tupOutcome.getCost() * 0.85){
                    hitChanceSum += chance;
                }
            }

        }


        //categoryMarker
        if(categoryEven > categoryProfit * 2.5) tupOutcome.setCategoryMarker((byte) 1);
        tupOutcome.setOutcome((skinAvgPrice - this.tupOutcome.getCost()) / this.tupOutcome.getCost());
        tupOutcome.setChanceValue(skinAvgPrice - this.tupOutcome.getCost());
        tupOutcome.setLoss(this.tupOutcome.getCost() - skinMinPrice);
        tupOutcome.setHitChance(hitChanceSum);
        tupOutcome.setValue(skinMaxPrice - this.tupOutcome.getCost());
        tupOutcome.setRepeatFactor(tupOutcome.getValue() / tupOutcome.getLoss());
        tupOutcome.setRepeatFactorChance(skinAvgPrice / tupOutcome.getLoss());
        tupOutcome.setSkinAvg(skinAvgPrice);
        tupOutcome.setSkinMin(skinMinPrice);
        tupOutcome.setSkinMax(skinMaxPrice);




        //store updated values
        for(Set<TradeUpOutcomeSkinsRecord> outSkins : outSkinsMap.values()){
            outSkins.forEach(UpdatableRecordImpl::store);
        }

        this.setStatus(TradeUpStatus.CALCULATED);
        this.tupOutcome.store();

        if(!csMoneyPrice){
            if(skinMaxPrice < this.tupOutcome.getCost() * 1.15){
                this.setStatus(TradeUpStatus.WASTED);
                this.store();
                return;
            }
        }


        if(tupOutcome.getLoss() > tupOutcome.getValue()){
            this.setStatus(TradeUpStatus.WASTED);
            this.store();
            return;
        }


        if(tupOutcome.getValue() < 0.5 ){
            this.setStatus(TradeUpStatus.WASTED);
            this.store();
            return;
        }
        this.store();
        log.info("Calculated TradeUp id: "+this.getId());

    }


    //TODO doc
    public static void reCalculateUpdatedPrices(List<TradeUpCustom> tupList) {
        log.info("updating tups: {}", tupList.size());
        tupLoop:
        for (TradeUpCustom tup : tupList) {
            QRTradeUpGenerated.updateTradeUpSkins(tup.getId());
            TradeUpSettings settings = QRGenerationSettings.getTradeUpSettings(tup.getGenerationSettingsId());
            double totalPrice = 0;

            List<String> collList = settings.getCollectionList();
            List<Condition> condList = settings.getConditionList();
            List<Integer> collNumber = settings.getCollNumber();
            for (int i = 0; i < collList.size(); i++) {
                String collectionName = collList.get(i);
                Condition condition = condList.get(i);
                if (collNumber.get(i) == 0) {
                    continue;
                }
                double minSkinPriceAvg = QRCS2Skin.getTradeUpSkinsAveragePrice(false, collectionName, condition, tup.getId());

                if (minSkinPriceAvg <= 0) {
                    QRTradeUpGenerated.updateStatus(tup.getId(), TradeUpStatus.WASTED);
                    continue tupLoop;
                }

                totalPrice += collNumber.get(i) * minSkinPriceAvg;
            }
            // TODO maybe remove if it cant happen
            if (totalPrice <= 0) throw new RuntimeException("totalPrice is 0");
            TradeUpOutcomeRecord tupOutcome = QRTradeUpGenerated.getTradeUpOutcome(tup.getId());
            if(tupOutcome == null) throw new RuntimeException("tupOutcome is null");
            tupOutcome.setCost(totalPrice*costMultiplier);


            List<TradeUpOutcomeSkinsRecord> outSkins = QRTradeUpGenerated.getTradeUpOutcomeSkins(tup.getId());

            //TODO only single collection!
            //TODO only single collection!
            double skinPool = 10 * outSkins.size();
            double chance = 10 / skinPool;

            double hitChanceSum = 0;
            double skinAvgPrice = 0;
            double skinMinPrice = Double.MAX_VALUE, skinMaxPrice = Double.MIN_VALUE;
            double categoryEven = 0, categoryProfit = 0;
            for (TradeUpOutcomeSkinsRecord skin : outSkins) {

                double skinPrice = QRSkinPrice.getSkinPrice(skin.getCS2SkinId()) * outSkinMultiplier;

                skinAvgPrice += skinPrice * chance;
                if (skinPrice > skinMaxPrice) {
                    skinMaxPrice = skinPrice;
                }
                if (skinPrice < skinMinPrice) {
                    skinMinPrice = skinPrice;
                }
                //categoryMarker
                if (skinPrice > tupOutcome.getCost() * 1.15) {
                    categoryProfit += chance;
                } else if (skinPrice >= tupOutcome.getCost() * 0.85 && skinPrice <= tupOutcome.getCost() * 1.15) {
                    categoryEven += chance;
                }
                //hitChance
                if (skinPrice >= tupOutcome.getCost() * 0.90) {
                    hitChanceSum += chance;
                }

            }
            if (categoryEven > categoryProfit * 2.5) tupOutcome.setCategoryMarker((byte) 1);
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
            tupOutcome.store();

            if(tupOutcome.getOutcome()<0){
                QRTradeUpGenerated.updateStatus(tup.getId(), TradeUpStatus.WASTED);
            }else {
                QRTradeUpGenerated.updateStatus(tup.getId(), TradeUpStatus.CALCULATED);
            }

        }
    }


    private void checkFloatMarker(double skinFloat){
        if(skinFloat >= 0.055 &&  skinFloat < 0.075){
           this.tupOutcome.setFloatMarker((byte) 1);
        } else if (skinFloat >= 0.13 &&  skinFloat < 0.165) {
            this.tupOutcome.setFloatMarker((byte) 1);
        } else if (skinFloat >= 0.35 &&  skinFloat < 0.47) {
            this.tupOutcome.setFloatMarker((byte) 1);
        }
    }
}
