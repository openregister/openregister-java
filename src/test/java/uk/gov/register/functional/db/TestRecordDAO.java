package uk.gov.register.functional.db;

import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.Define;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;
import org.skife.jdbi.v2.sqlobject.stringtemplate.UseStringTemplate3StatementLocator;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

@UseStringTemplate3StatementLocator
public interface TestRecordDAO {
    @SqlUpdate("delete from \"<schema>\".current_keys;" +
            "delete from \"<schema>\".total_records;" +
            "insert into \"<schema>\".total_records values(0)")
    void wipeData(@Define("schema") String schema);

    @RegisterMapper(RecordMapper.class)
    @SqlQuery("select key,entry_number from \"<schema>\".current_keys where key = :primaryKey")
    TestRecord getRecord(@Bind("primaryKey") String primaryKey, @Define("schema") String schema);

    class RecordMapper implements ResultSetMapper<TestRecord> {
        @Override
        public TestRecord map(int index, ResultSet r, StatementContext ctx) throws SQLException {
            return new TestRecord(r.getString("key"), r.getInt("entry_number"));
        }
    }
}

