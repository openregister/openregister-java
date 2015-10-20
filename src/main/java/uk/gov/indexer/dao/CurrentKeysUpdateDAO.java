package uk.gov.indexer.dao;

import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.stringtemplate.UseStringTemplate3StatementLocator;
import org.skife.jdbi.v2.unstable.BindIn;

import java.util.Set;

@UseStringTemplate3StatementLocator
interface CurrentKeysUpdateDAO extends DBConnectionDAO {
    String CURRENT_KEYS_TABLE = "CURRENT_KEYS";

    @SqlUpdate("CREATE TABLE IF NOT EXISTS " + CURRENT_KEYS_TABLE + " (KEY VARCHAR PRIMARY KEY, SERIAL_NUMBER INTEGER UNIQUE)")
    void ensureCurrentKeysTableExists();

    @SqlUpdate("UPDATE " + CURRENT_KEYS_TABLE + " SET SERIAL_NUMBER=:serial_number WHERE KEY=:key")
    int updateSerialNumber(@Bind("serial_number") int serial_number, @Bind("key") String key);

    @SqlQuery("SELECT KEY FROM " + CURRENT_KEYS_TABLE + " WHERE KEY IN (<keys>)")
    Set<String> getExistingKeys(@BindIn("keys") Iterable<String> keys);

    @SqlUpdate("INSERT INTO " + CURRENT_KEYS_TABLE + "(SERIAL_NUMBER, KEY) VALUES(:serial_number, :key)")
    void insertNewKey(@Bind("serial_number") int serial_number, @Bind("key") String key);
}

