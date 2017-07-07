package uk.gov.register.functional.db;

import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.Define;
import org.skife.jdbi.v2.sqlobject.stringtemplate.UseStringTemplate3StatementLocator;

@UseStringTemplate3StatementLocator
public interface TestIndexDAO {
    @SqlUpdate("delete from \"<schema>\".index;")
    void wipeData(@Define("schema") String schema);
}
