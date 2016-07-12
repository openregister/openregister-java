package uk.gov.register.presentation.functional.testSupport;

import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;

public interface TestCurrentKeyDAO {
    @SqlUpdate("drop table if exists current_keys")
    void dropTable();

    @SqlUpdate("create table if not exists current_keys (serial_number integer primary key, key varchar unique)")
    void createTable();

    @SqlUpdate("update current_keys set serial_number=:serial_number where key=:key")
    void update(@Bind("key") String key, @Bind("serial_number") int serialNumber);

    @SqlUpdate("insert into current_keys(serial_number,key) values(:serial_number, :key)")
    void insert(@Bind("key") String key, @Bind("serial_number") int serialNumber);

    @SqlQuery("select serial_number from current_keys where key=:key")
    int getSerialNumber(@Bind("key") String key);
}
