package uk.gov.functional.db;

import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public interface TestItemDAO {
    @SqlUpdate("drop table if exists item")
    void dropTable();

    @RegisterMapper(ItemMapper.class)
    @SqlQuery("select * from item")
    List<TestDBItem> getItems();

    class ItemMapper implements ResultSetMapper<TestDBItem> {
        @Override
        public TestDBItem map(int index, ResultSet r, StatementContext ctx) throws SQLException {
            return new TestDBItem(r.getString("sha256hex"), r.getBytes("content"));
        }
    }
}

