package uk.gov.register.presentation.functional.testSupport;

import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;

import java.time.Instant;

public interface TestEntryDAO {
    @SqlUpdate("drop table if exists entry")
    void dropTable();

    @SqlUpdate("create table if not exists entry (entry_number integer primary key, sha256hex varchar, timestamp timestamp without time zone)")
    void createTable();

    @SqlUpdate("insert into entry(entry_number, sha256hex, timestamp) values(:entry_number, :sha256hex, :timestamp)")
    void insert(@Bind("entry_number") int serialNumber, @Bind("sha256hex") String sha256, @Bind("timestamp") Instant timestamp);
}
