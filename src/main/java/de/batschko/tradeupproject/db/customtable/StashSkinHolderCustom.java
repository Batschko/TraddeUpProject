package de.batschko.tradeupproject.db.customtable;

import de.batschko.tradeupproject.enums.Rarity;
import de.batschko.tradeupproject.tables.records.StashSkinHolderRecord;

import java.time.LocalDateTime;

/**
 * Class to extend {@link StashSkinHolderRecord}.
 */
public class StashSkinHolderCustom extends StashSkinHolderRecord {

    public StashSkinHolderCustom() {
        super();
    }

    /**
     * Instantiates a new {@link StashSkinHolderCustom}.
     *
     * @param stashId    stash id
     * @param caseCollId case/coll id
     * @param weapon     weapon text
     * @param title      title text
     * @param rarity     {@link Rarity} rarity
     * @param floatMin   float min
     * @param floatMax   float max
     * @param imageUrl   image url
     */
    public StashSkinHolderCustom(int stashId, int caseCollId, String weapon, String title, Rarity rarity, double floatMin, double floatMax, String imageUrl) {
        super(stashId,LocalDateTime.now(),caseCollId,weapon,title,rarity,null,null,floatMin,floatMax,imageUrl);
    }


}
