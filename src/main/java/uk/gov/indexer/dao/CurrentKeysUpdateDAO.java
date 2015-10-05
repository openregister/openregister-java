package uk.gov.indexer.dao;

import org.skife.jdbi.v2.sqlobject.*;

import java.util.Iterator;
import java.util.List;

interface CurrentKeysUpdateDAO extends DBConnectionDAO {
    String CURRENT_KEYS_TABLE = "CURRENT_KEYS";

    @SqlUpdate("CREATE TABLE IF NOT EXISTS " + CURRENT_KEYS_TABLE + " (KEY VARCHAR PRIMARY KEY, SERIAL_NUMBER INTEGER UNIQUE)")
    void ensureCurrentKeysTableExists();

    @SqlUpdate("UPDATE " + CURRENT_KEYS_TABLE + " SET SERIAL_NUMBER=:serial_number WHERE KEY=:key")
    int updateSerialNumber(@Bind("serial_number") int serial_number, @Bind("key") String key);

    @SqlQuery("SELECT KEY FROM " + CURRENT_KEYS_TABLE + " WHERE KEY IN (:keys)")
    List<String> getExistingKeys(@Bind("keys") String keys);

    @SqlBatch("insert into " + CURRENT_KEYS_TABLE + "(SERIAL_NUMBER, KEY) values(:serial_number, :primaryKey)")
    void insertEntries(@BindBean Iterator<OrderedIndexEntry> entries);
}

