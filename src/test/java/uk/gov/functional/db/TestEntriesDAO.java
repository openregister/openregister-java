package uk.gov.functional.db;

import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;

public interface TestEntriesDAO {
    @SqlUpdate("create table if not exists entries (id serial primary key, entry bytea)")
    void createTable();

    @SqlUpdate("drop table if exists entries")
    void dropTable();

    @SqlQuery("select entry from entries limit 1")
    byte[] getEntry();

    @SqlUpdate("insert into entries(entry) values(:entry)")
    void load(@Bind("entry") byte[] entry);
}
