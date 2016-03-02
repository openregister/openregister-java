package uk.gov.functional.db;

import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;

public interface TestEntriesDAO {
    @SqlUpdate("drop table if exists entries")
    void dropTable();

    @SqlQuery("select entry from entries limit 1")
    byte[] getEntry();
}
