package de.batschko.tradeupproject.db.customtable;

import de.batschko.tradeupproject.enums.Condition;
import de.batschko.tradeupproject.tables.records.CS2SkinRecord;

import java.time.LocalDateTime;

/**
 * Class to extend {@link CS2SkinRecord}.
 */
public class CS2SkinCustom extends CS2SkinRecord {

    public CS2SkinCustom() {
        super();
    }

    /**
     * Instantiates a new {@link CS2SkinCustom}.
     *
     * @param stashId   stash id
     * @param stattrak  stattrak as byte
     * @param condition {@link Condition} condition
     */

    public CS2SkinCustom(Integer stashId, byte stattrak, Condition condition) {
        super(null, stashId, LocalDateTime.now(), stattrak, condition, null);
    }

}
