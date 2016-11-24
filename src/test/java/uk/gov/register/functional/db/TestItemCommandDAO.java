package uk.gov.register.functional.db;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;
import org.skife.jdbi.v2.tweak.ResultSetMapper;
import uk.gov.register.core.HashingAlgorithm;
import uk.gov.register.util.HashValue;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public interface TestItemCommandDAO {
    @SqlUpdate("delete from item")
    void wipeData();

    @RegisterMapper(ItemMapper.class)
    @SqlQuery("select * from item")
    List<TestDBItem> getItems();

    class ItemMapper implements ResultSetMapper<TestDBItem> {

        private final ObjectMapper objectMapper = new ObjectMapper();

        @Override
        public TestDBItem map(int index, ResultSet r, StatementContext ctx) throws SQLException {
            try {
                return new TestDBItem(new HashValue(HashingAlgorithm.SHA256, r.getString("sha256hex")), objectMapper.readTree(r.getString("content")));
            } catch (IOException e) {
                throw new SQLException(e);
            }
        }
    }
}

