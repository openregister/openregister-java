package uk.gov.functional.db;

import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;

public interface TestEntryDAO {
    @SqlUpdate("drop table entry")
    void dropTable();

    @SqlQuery("select sha256hex from entry")
    String getHex();
}
