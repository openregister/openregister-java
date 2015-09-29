package uk.gov.indexer.dao;

import org.skife.jdbi.v2.sqlobject.SqlUpdate;

interface TotalRegisterEntriesUpdateDAO extends DBConnectionDAO {
    String TOTAL_REGISTER_ENTRIES_TABLE = "register_entries_count";

    @SqlUpdate("CREATE TABLE IF NOT EXISTS " + TOTAL_REGISTER_ENTRIES_TABLE + " (COUNT INTEGER)")
    void ensureTotalEntriesInRegisterTableExists();

    @SqlUpdate("INSERT INTO " + TOTAL_REGISTER_ENTRIES_TABLE + "(COUNT) SELECT 0 WHERE NOT EXISTS (SELECT 1 FROM " + TOTAL_REGISTER_ENTRIES_TABLE + ")")
    void initialiseTotalEntriesInRegisterIfRequired();

    @SqlUpdate("UPDATE " + TOTAL_REGISTER_ENTRIES_TABLE + " SET COUNT=COUNT+1")
    void increaseTotalEntriesInRegisterCount();
}
