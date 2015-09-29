package uk.gov.indexer.dao;

import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;

interface WatermarkUpdateDAO {
    String WATERMARK_TABLE = "STREAMED_ENTRIES";

    @SqlUpdate("CREATE TABLE IF NOT EXISTS " + WATERMARK_TABLE + " (SERIAL_NUMBER INTEGER PRIMARY KEY, TIME TIMESTAMP)")
    void ensureWaterMarkTableExists();

    @SqlUpdate("INSERT INTO " + WATERMARK_TABLE + "(SERIAL_NUMBER) SELECT 0 WHERE NOT EXISTS (SELECT 1 FROM " + WATERMARK_TABLE + ")")
    void initialiseWaterMarkTableIfRequired();

    @SqlUpdate("UPDATE " + WATERMARK_TABLE + " SET SERIAL_NUMBER=SERIAL_NUMBER+1, TIME=NOW()")
    int increaseWaterMarkByOne();

    @SqlQuery("SELECT SERIAL_NUMBER FROM " + WATERMARK_TABLE)
    int currentWaterMark();
}
