package de.batschko.tradeupproject.db.query;

import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;


/**
 * Abstract base class for database access, providing a shared {@link DSLContext}.
 *
 * <p>Uses Spring's dependency injection of {@link DSLContext}.</p>
 */
@Repository
public abstract class QueryRepository {

    /**
     * Shared DSLContext for database queries.
     */
    protected static DSLContext dsl;


    /**
     * Initializes {@code QueryRepository} with a DSLContext.
     *
     * @param dslContext Injected DSLContext for database operations.
     */
    @Autowired
    public QueryRepository(DSLContext dslContext) {
        dsl = dslContext;
    }



}
