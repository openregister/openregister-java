package uk.gov.register.presentation.functional.testSupport;

import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;

public interface TestTotalRecordDAO {
    @SqlUpdate("drop table if exists total_records")
    void dropTable();

    @SqlUpdate("create table if not exists total_records (count integer); insert into total_records(count) values(0);")
    void createTable();

    @SqlUpdate("Update total_records set count=count+:num")
    void updateBy(@Bind("num") int number);
}
