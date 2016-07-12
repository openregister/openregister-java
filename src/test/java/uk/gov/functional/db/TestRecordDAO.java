package uk.gov.functional.db;

import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public interface TestRecordDAO {
    @SqlUpdate("delete from current_keys;" +
            "delete from total_records;" +
            "insert into total_records values(0)")
    void wipeData();

    @RegisterMapper(RecordMapper.class)
    @SqlQuery("select key,entry_number from current_keys where key = :primaryKey")
    TestRecord getRecord(@Bind("primaryKey") String primaryKey);

    class RecordMapper implements ResultSetMapper<TestRecord> {
        @Override
        public TestRecord map(int index, ResultSet r, StatementContext ctx) throws SQLException {
            return new TestRecord(r.getString("key"), r.getInt("entry_number"));
        }
    }
}

