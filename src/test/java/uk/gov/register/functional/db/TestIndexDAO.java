package uk.gov.register.functional.db;

import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.OverrideStatementRewriterWith;
import uk.gov.register.db.SubstituteSchemaRewriter;

@OverrideStatementRewriterWith(SubstituteSchemaRewriter.class)
public interface TestIndexDAO {
    @SqlUpdate("delete from :schema.index;")
    void wipeData(@Bind("schema") String schema);
}
