package uk.gov.register.functional.testSupport;

import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;

public interface TestTotalRecordDAO {
    @SqlUpdate("Update total_records set count=count+:num")
    void updateBy(@Bind("num") int number);
}
