package uk.gov.store;

import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;

public interface EntriesUpdateDAO {
    String tableName = "entries";

    @SqlUpdate("CREATE TABLE IF NOT EXISTS " + tableName + " (ID SERIAL PRIMARY KEY, ENTRY BYTEA)")
    void ensureTableExists();

    @SqlUpdate("INSERT INTO " + tableName + "(ENTRY) values(:message)")
    void add(@Bind("message") byte[] message);
}
