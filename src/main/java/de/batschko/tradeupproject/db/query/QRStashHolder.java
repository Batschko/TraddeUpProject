package de.batschko.tradeupproject.db.query;


import de.batschko.tradeupproject.enums.Rarity;
import de.batschko.tradeupproject.tables.StashSkinHolder;
import de.batschko.tradeupproject.tables.records.StashSkinHolderRecord;
import org.jooq.DSLContext;
import org.jooq.Record4;
import org.jooq.Result;
import org.springframework.stereotype.Repository;

import java.util.List;

import static de.batschko.tradeupproject.tables.Collection.COLLECTION;
import static de.batschko.tradeupproject.tables.StashSkinHolder.STASH_SKIN_HOLDER;

/**
 * Database access related to {@link StashSkinHolder}
 */
@Repository
public class QRStashHolder extends QueryRepository{

    public QRStashHolder(DSLContext dslContext) {
        super(dslContext);
    }

    /**
     * Get all {@link StashSkinHolder}s .
     *
     * @return list of {@link StashSkinHolder}
     */
    public static List<StashSkinHolder> getAll(){
        return dsl.select()
                .from(STASH_SKIN_HOLDER)
                .fetchInto(StashSkinHolder.class);
    }

    /**
     * Get all {@link StashSkinHolder}s with limit.
     *
     * @param limit limit
     * @return list of {@link StashSkinHolder}
     */
    public static List<StashSkinHolder> getAll(int limit){
        return dsl.select()
                .from(STASH_SKIN_HOLDER)
                .limit(limit)
                .fetchInto(StashSkinHolder.class);
    }

    /**
     * Gets {@link StashSkinHolderRecord}s for given collection and rarity.
     *
     * @param collName collection name
     * @param rarity   rarity {@link Rarity}
     * @return list of {@link StashSkinHolderRecord}s
     */
    public static List<StashSkinHolderRecord> getByCollectionRarity(String collName, Rarity rarity) {
        return dsl.select(STASH_SKIN_HOLDER.fields())
                .from(STASH_SKIN_HOLDER)
                .join(COLLECTION)
                .on(STASH_SKIN_HOLDER.COLLECTION_ID.eq(COLLECTION.ID))
                .where(COLLECTION.COLL_NAME.eq(collName))
                .and(STASH_SKIN_HOLDER.RARITY.eq(rarity))
                .fetchInto(StashSkinHolderRecord.class);
    }

    /**
     * Get the highest {@link Rarity} collection name.
     *
     * @param collname collection name
     * @return {@link Rarity}
     */
    public static Rarity getHighestRarityByCollName(String collname){
        return dsl.select(STASH_SKIN_HOLDER.RARITY)
                .from(STASH_SKIN_HOLDER)
                .join(COLLECTION)
                .on(STASH_SKIN_HOLDER.COLLECTION_ID.eq(COLLECTION.ID))
                .where(COLLECTION.COLL_NAME.eq(collname))
                .and(STASH_SKIN_HOLDER.TOP.eq((byte)1))
                .fetchAnyInto(Rarity.class);
    }

    /**
     * Get the lowest {@link Rarity} by collection name.
     *
     * @param collName collection name
     * @return {@link Rarity}
     */
    public static Rarity getLowestRarityByCollName(String collName){
        return dsl.select(STASH_SKIN_HOLDER.RARITY)
                .from(STASH_SKIN_HOLDER)
                .join(COLLECTION)
                .on(STASH_SKIN_HOLDER.COLLECTION_ID.eq(COLLECTION.ID))
                .where(COLLECTION.COLL_NAME.eq(collName))
                .and(STASH_SKIN_HOLDER.BOTTOM.eq((byte)1))
                .fetchAnyInto(Rarity.class);
    }

    /**
     * Get cs2 skin info (stashId, floatStart, floatEnd, isCase).
     *
     * @return Result-Record4 (Integer, Double, Double, Byte) -->
     * stashId, floatStart, floatEnd, isCase (Integer, Double, Double, Byte)
     */
    public static Result<Record4<Integer, Double, Double, Byte>> getCS2SkinInfo(){
        return dsl.select(STASH_SKIN_HOLDER.STASH_ID, STASH_SKIN_HOLDER.FLOAT_START, STASH_SKIN_HOLDER.FLOAT_END, COLLECTION.IS_CASE)
                .from(STASH_SKIN_HOLDER)
                .join(COLLECTION)
                .on(STASH_SKIN_HOLDER.COLLECTION_ID.eq(COLLECTION.ID))
                .fetch();
    }
}