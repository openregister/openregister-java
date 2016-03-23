package uk.gov.register.presentation.functional.testSupport;

import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;

public interface TestTotalEntryDAO {
    @SqlUpdate("drop table if exists total_entries")
    void dropTable();

    @SqlUpdate("create table if not exists total_entries (count integer, last_updated timestamp without time zone default now()); insert into total_entries(count) values(0);")
    void createTable();

    @SqlUpdate("Update total_entries set count=count+:num")
    void updateBy(@Bind("num") int number);}
