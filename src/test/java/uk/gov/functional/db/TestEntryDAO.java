package uk.gov.functional.db;

import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;

import java.util.List;

public interface TestEntryDAO {
    @SqlUpdate("drop table if exists entry")
    void dropTable();

    @SqlQuery("select sha256hex from entry")
    List<String> getAllHex();
}
