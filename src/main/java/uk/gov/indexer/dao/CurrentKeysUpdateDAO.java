package uk.gov.indexer.dao;

import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;

interface CurrentKeysUpdateDAO {
    String CURRENT_KEYS_TABLE = "CURRENT_KEYS";

    @SqlUpdate("CREATE TABLE IF NOT EXISTS " + CURRENT_KEYS_TABLE + " (KEY VARCHAR PRIMARY KEY, SERIAL_NUMBER INTEGER UNIQUE)")
    void ensureCurrentKeysTableExists();


    @SqlUpdate("INSERT INTO " + CURRENT_KEYS_TABLE + "(SERIAL_NUMBER, KEY) VALUES(:serial_number, :key)")
    int writeCurrentKey(@Bind("serial_number") int serial_number, @Bind("key") String key);

    @SqlUpdate("UPDATE " + CURRENT_KEYS_TABLE + " SET SERIAL_NUMBER=:serial_number WHERE KEY=:key")
    int updateCurrentKey(@Bind("serial_number") int serial_number, @Bind("key") String key);

    @SqlQuery("SELECT CASE " +
            "WHEN EXISTS(" +
            "SELECT 1 FROM " + CURRENT_KEYS_TABLE + " WHERE KEY=:key" +
            ") " +
            "THEN(" +
            "SELECT SERIAL_NUMBER FROM " + CURRENT_KEYS_TABLE + " WHERE KEY=:key" +
            ") " +
            "ELSE -1 " +
            "END")
    int getCurrentSerialNumberForKey(@Bind("key") String key);
}

