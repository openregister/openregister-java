package uk.gov.indexer.dao;

import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;

interface WatermarkUpdateDao {
    String WATERMARK_TABLE = "STREAMED_ENTRIES";

    @SqlUpdate("CREATE TABLE IF NOT EXISTS " + WATERMARK_TABLE + " (ID INTEGER PRIMARY KEY, TIME TIMESTAMP)")
    void ensureWaterMarkTableExists();

    @SqlUpdate("INSERT INTO " + WATERMARK_TABLE + "(ID) SELECT 0 WHERE NOT EXISTS (SELECT 1 FROM " + WATERMARK_TABLE + ")")
    void initialiseWaterMarkTableIfRequired();

    @SqlUpdate("UPDATE " + WATERMARK_TABLE + " SET ID=ID+1, TIME=NOW()")
    int increaseWaterMarkByOne();

    @SqlQuery("SELECT ID FROM " + WATERMARK_TABLE)
    int currentWaterMark();
}
