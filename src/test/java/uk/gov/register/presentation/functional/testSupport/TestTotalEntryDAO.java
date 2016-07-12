package uk.gov.register.presentation.functional.testSupport;

import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;

public interface TestTotalEntryDAO {
    @SqlUpdate("drop table if exists current_entry_number")
    void dropTable();

    @SqlUpdate("create table if not exists current_entry_number (value integer); insert into current_entry_number(value) values(0);")
    void createTable();

    @SqlUpdate("Update current_entry_number set value=value+:num")
    void updateBy(@Bind("num") int number);}
