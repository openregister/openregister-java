package uk.gov.register.presentation.dao;

import org.postgresql.util.PGobject;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;

public interface RecentEntryIndexUpdateDAO {
    @SqlUpdate("CREATE TABLE IF NOT EXISTS ordered_entry_index (ID SERIAL PRIMARY KEY, ENTRY JSONB)")
    void ensureTableExists();

    @SqlUpdate("INSERT INTO ordered_entry_index (ENTRY) VALUES (:entry)")
    void append(@Bind("entry") PGobject entry);
}
