package uk.gov.register.db;

import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.stringtemplate.UseStringTemplate3StatementLocator;

@UseStringTemplate3StatementLocator("/sql/ensure_schema.sql")
public interface SchemaCreator {
    @SqlUpdate
    void ensureSchema();
}
