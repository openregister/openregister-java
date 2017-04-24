package uk.gov.register.functional.db;

import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.OverrideStatementLocatorWith;
import uk.gov.register.db.SchemaRewriter;

@OverrideStatementLocatorWith(SchemaRewriter.class)
public interface TestIndexDAO {
    @SqlUpdate("delete from :schema.index;")
    void wipeData();
}
