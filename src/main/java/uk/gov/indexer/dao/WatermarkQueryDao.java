package uk.gov.indexer.dao;

import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;

interface WatermarkQueryDao {
    static final String WATERMARK_TABLE = "STREAMED_ENTRIES";

    @SqlUpdate("CREATE TABLE IF NOT EXISTS " + WATERMARK_TABLE + " (ID INTEGER PRIMARY KEY, TIME TIMESTAMP)")
    public abstract void ensureWaterMarkTableExists();

    @SqlUpdate("INSERT INTO " + WATERMARK_TABLE + "(ID) SELECT 0 WHERE NOT EXISTS (SELECT 1 FROM " + WATERMARK_TABLE + ")")
    public abstract void initialiseWaterMarkTableIfRequired();

    @SqlUpdate("UPDATE " + WATERMARK_TABLE + " SET ID=ID+1, TIME=NOW()")
    abstract int increaseWaterMarkByOne();

    @SqlQuery("SELECT ID FROM " + WATERMARK_TABLE)
    public abstract int currentWaterMark();
}
