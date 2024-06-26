package de.batschko.tradeupproject.tradeup;

import de.batschko.tradeupproject.db.query.QRCollection;
import de.batschko.tradeupproject.enums.Condition;
import de.batschko.tradeupproject.enums.Rarity;
import de.batschko.tradeupproject.tables.records.GenerationSettingsRecord;
import de.batschko.tradeupproject.utils.SkinUtils;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * Stores settings for a TradeUp, acts as mapper for {@link GenerationSettingsRecord}
 */
public class TradeUpSettings extends CollectionConditionDistribution {

    @Getter
    List<String> collectionList;
    Condition condTarget;


    /**
     * Instantiates a new {@link TradeUpSettings}.
     *
     * @param collectionList collection list
     * @param collCondDistri collection and condition distribution
     * @param condTarget     condition target
     */
    public TradeUpSettings(List<String> collectionList,CollectionConditionDistribution collCondDistri, Condition condTarget) {
        super(collCondDistri.getCollNumber(), collCondDistri.getConditionList());
        this.collectionList = collectionList;
        this.condTarget = condTarget;
    }

    /**
     * Instantiates a new {@link TradeUpSettings}.
     *
     * @param collectionList     collection list
     * @param numberDistribution number distribution
     * @param conditionList      condition list
     * @param condTarget         condition target
     */
    public TradeUpSettings(List<String> collectionList,List<Integer> numberDistribution,List<Condition> conditionList, Condition condTarget) {
        super(numberDistribution, conditionList);
        this.collectionList = collectionList;
        this.condTarget = condTarget;
    }

    @Override
    public String toString() {
        return "TradeUpSettings{" +
                "collectionList=" + collectionList +
                ", condTarget=" + condTarget +
                ", collNumber=" + collNumber +
                ", conditionList=" + conditionList +
                '}';
    }


    /**
     * Gets collection list index for specific collection.
     *
     * @param searchString the collection to search for index
     * @return the collection list index
     */
    public int getCollectionListIndex(String searchString) {
        for (int i = 0; i < collectionList.size(); i++) {
            if (collectionList.get(i).equals(searchString)) {
                return i;
            }
        }
        throw new RuntimeException("Couldn't get CollectionList Index for: "+searchString+" in: "+collectionList);
    }

    /**
     * Get collection count.
     *
     * @return collection count
     */
    public byte getCollectionCount(){
        byte collCount = (byte) collectionList.size();
        if(collCount == 2){
            if(collectionList.getFirst().equals(collectionList.getLast())){
               return 1;
            }
        }
        return collCount;
    }

    /**
     * Create list of {@link SkinUtils.TradeUpSkinInfo}.
     * <p>(DTOs to find tradeup skins)</p>
     * @param rarity   rarity {@link Rarity}
     * @param stattrak stattrak as byte
     * @return list of {@link SkinUtils.TradeUpSkinInfo}
     */
    public List<SkinUtils.TradeUpSkinInfo> getTradeUpSkinInfo(Rarity rarity, byte stattrak){
        List<SkinUtils.TradeUpSkinInfo> tradeUpSkinInfos = new ArrayList<>();
        for(int i=0; i< collectionList.size(); i++){
            if(collNumber.get(i) == 0){
                continue;
            }
            tradeUpSkinInfos.add(new SkinUtils.TradeUpSkinInfo(collectionList.get(i),conditionList.get(i),rarity,stattrak));
        }
        return tradeUpSkinInfos;
    }

    /**
     * Returns true if {@link TradeUpSettings} has a collection in collectionList, false if only cases are in the list
     *
     * @return true if a collection in collectionList
     */
    public boolean hasCollection(){
        boolean hasColl = false;
        for(String coll : collectionList){
            if(!QRCollection.isCase(coll)) hasColl = true;
        }
        return hasColl;
    }

    /**
     * Serialize {@link TradeUpSettings} to string.
     *
     * @return serialized String
     */
    public String serialize(){
        StringBuilder sb = new StringBuilder();
        for(String collection: collectionList){
            sb.append(collection).append(",");
        }
        sb.deleteCharAt(sb.length()-1);
        sb.append(";");
        for(int number: collNumber){
            sb.append(number).append(",");
        }
        sb.deleteCharAt(sb.length()-1);
        sb.append(";");
        for(Condition condition: conditionList){
            sb.append(condition).append(",");
        }
        sb.deleteCharAt(sb.length()-1);
        sb.append(";");
        sb.append(condTarget);
        return sb.toString();
    }


    /**
     * Deserialize String to {@link TradeUpSettings}.
     *
     * @param s serialized string
     * @return {@link TradeUpSettings}
     */
    public static TradeUpSettings deserialize(String s){
        String[] strings = s.split(";");
        String collections = strings[0];
        String numberDistribution = strings[1];
        String conditions = strings[2];
        String conditionTarget = strings[3];

        //create collectionList
        List<String> collectionList = Arrays.stream(collections.split(",")).toList();

        //create numberDistributionList
        String[] numberDistributionArray = numberDistribution.split(",");
        List<Integer> numberDistributionList = new ArrayList<>();

        for (String item : numberDistributionArray){
            numberDistributionList.add(Integer.parseInt(item));
        }

        //create conditionsList
        List<Condition> conditionsList = new ArrayList<>();
        String[] conditionsArray = conditions.split(",");
        for (String item : conditionsArray){
            conditionsList.add(Condition.valueOf(item));
        }
        return new TradeUpSettings(collectionList, numberDistributionList, conditionsList, Condition.valueOf(conditionTarget));
    }


}
