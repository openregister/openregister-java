package uk.gov.register.functional.db;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.Define;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;
import org.skife.jdbi.v2.sqlobject.stringtemplate.UseStringTemplate3StatementLocator;
import org.skife.jdbi.v2.tweak.ResultSetMapper;
import uk.gov.register.core.HashingAlgorithm;
import uk.gov.register.util.HashValue;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@UseStringTemplate3StatementLocator
public interface TestBlobCommandDAO {
    @SqlUpdate("delete from \"<schema>\".item")
    void wipeData(@Define("schema") String schema);

    @RegisterMapper(BlobMapper.class)
    @SqlQuery("select * from \"<schema>\".item")
    List<TestDBBlob> getItems(@Define("schema") String schema);

    class BlobMapper implements ResultSetMapper<TestDBBlob> {

        private final ObjectMapper objectMapper = new ObjectMapper();

        @Override
        public TestDBBlob map(int index, ResultSet r, StatementContext ctx) throws SQLException {
            try {
                return new TestDBBlob(new HashValue(HashingAlgorithm.SHA256, r.getString("sha256hex")), objectMapper.readTree(r.getString("content")));
            } catch (IOException e) {
                throw new SQLException(e);
            }
        }
    }
}

