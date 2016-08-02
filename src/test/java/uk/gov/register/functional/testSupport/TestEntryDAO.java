package uk.gov.register.functional.testSupport;

import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;

import java.time.Instant;

public interface TestEntryDAO {
    @SqlUpdate("insert into entry(entry_number, sha256hex, timestamp) values(:entry_number, :sha256hex, :timestamp)")
    void insert(@Bind("entry_number") int serialNumber, @Bind("sha256hex") String sha256, @Bind("timestamp") Instant timestamp);
}
