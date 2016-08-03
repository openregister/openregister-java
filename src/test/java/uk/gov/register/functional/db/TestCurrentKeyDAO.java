package uk.gov.register.functional.db;

import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;

public interface TestCurrentKeyDAO {
    @SqlUpdate("update current_keys set entry_number=:entry_number where key=:key")
    void update(@Bind("key") String key, @Bind("entry_number") int serialNumber);

    @SqlUpdate("insert into current_keys(entry_number,key) values(:entry_number, :key)")
    void insert(@Bind("key") String key, @Bind("entry_number") int serialNumber);

    @SqlQuery("select entry_number from current_keys where key=:key")
    int getSerialNumber(@Bind("key") String key);
}
