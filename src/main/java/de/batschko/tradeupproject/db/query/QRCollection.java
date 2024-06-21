package de.batschko.tradeupproject.db.query;


import de.batschko.tradeupproject.tables.Collection;
import de.batschko.tradeupproject.tables.records.CollectionRecord;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import java.util.List;

import static de.batschko.tradeupproject.tables.Collection.COLLECTION;

/**
 * Database access related to {@link Collection}.
 */
@Repository
public class QRCollection extends QueryRepository {


    public QRCollection(DSLContext dsl) {
        super(dsl);
    }


    /**
     * Gets all {@link CollectionRecord}s.
     *
     * @return List of {@link CollectionRecord}s
     */
    public static List<CollectionRecord> getAll() {
        return dsl.select()
                .from(COLLECTION)
                .fetchInto(CollectionRecord.class);
    }


    /**
     * Gets collection names as String list.
     *
     * @return list of collection names
     */
    public static List<String> getAllCollectionNames() {
        return dsl.select(COLLECTION.COLL_NAME).from(COLLECTION).fetchInto(String.class);

    }


    /**
     * Gets collection id by collection name.
     *
     * @param collName collection name
     * @return collection id
     */
    public static int getCollectionId(String collName) {
        Integer result = dsl.select(COLLECTION.ID).from(COLLECTION).where(COLLECTION.COLL_NAME.eq(collName)).fetchOneInto(Integer.class);
        if (result == null) throw new RuntimeException("Couldn't get collectionId for name: " + collName);
        return result;
    }

    /**
     * Gets collection name by collection id.
     *
     * @param collId coll id
     * @return collection name
     */
    public static String getCollectionName(int collId) {
        String result = dsl.select(COLLECTION.COLL_NAME).from(COLLECTION).where(COLLECTION.ID.eq(collId)).fetchOneInto(String.class);
        if (result == null) throw new RuntimeException("Couldn't get name for collectionId: " + collId);
        return result;
    }

    /**
     * Checks id a collection is a case.
     *
     * @param collName collection name
     * @return true if collection is a case
     */
    public static boolean isCase(String collName) {
        try {
            return dsl.select(COLLECTION.IS_CASE).from(COLLECTION).where(COLLECTION.COLL_NAME.eq(collName)).fetchOneInto(Boolean.class);
        } catch (NullPointerException e) {
            throw new RuntimeException("Couldn't get isCase for collection: " + collName);
        }
    }

}