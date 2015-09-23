package uk.gov.indexer.dao;

import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;

interface CurrentKeysQueryDao{
    String CURRENT_KEYS_TABLE = "CURRENT_KEYS";


    @SqlUpdate("CREATE TABLE IF NOT EXISTS " + CURRENT_KEYS_TABLE + " (ID INTEGER PRIMARY KEY, KEY VARCHAR UNIQUE)")
    void ensureCurrentKeysTableExists();


    @SqlUpdate("INSERT INTO " + CURRENT_KEYS_TABLE + "(ID, KEY) VALUES(:id, :key)")
    int writeCurrentKey(@Bind("id") int id, @Bind("key") String key);

    @SqlUpdate("UPDATE " + CURRENT_KEYS_TABLE + " SET ID=:id WHERE KEY=:key")
    int updateCurrentKey(@Bind("id") int id, @Bind("key") String key);

    @SqlQuery("SELECT CASE " +
            "WHEN EXISTS(" +
            "SELECT 1 FROM " + CURRENT_KEYS_TABLE + " WHERE KEY=:key" +
            ") " +
            "THEN(" +
            "SELECT ID FROM " + CURRENT_KEYS_TABLE + " WHERE KEY=:key" +
            ") " +
            "ELSE -1 " +
            "END")
    int getCurrentIdForKey(@Bind("key") String key);
}
