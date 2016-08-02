package uk.gov.register.functional.testSupport;

import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;

public interface TestTotalEntryDAO {
    @SqlUpdate("Update current_entry_number set value=value+:num")
    void updateBy(@Bind("num") int number);
}
