package uk.gov.store;

import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;

public interface HighWaterMarkDAO {
    String tableName = "streamed_entries";

    @SqlUpdate("CREATE TABLE IF NOT EXISTS " + tableName + " (ID INTEGER PRIMARY KEY, TIME TIMESTAMP)")
    void ensureTableExists();

    @SqlUpdate("INSERT INTO " + tableName + " VALUES(:high_water_mark, now())")
    void updateHighWaterMark(@Bind("high_water_mark") int newHighWaterMark);

    @SqlQuery("SELECT ID FROM " + tableName + " ORDER BY ID DESC LIMIT 1")
    int getCurrentHighWaterMark();

    void close();
}
