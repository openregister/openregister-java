package uk.gov.functional.db;

import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;
import org.skife.jdbi.v2.tweak.ResultSetMapper;
import uk.gov.mint.Entry;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public interface TestEntryDAO {
    @SqlUpdate("drop table if exists entry;" +
            "drop table if exists current_entry_number")
    void dropTable();

    @RegisterMapper(EntryMapper.class)
    @SqlQuery("select entry_number,sha256hex from entry")
    List<Entry> getAllEntries();

    class EntryMapper implements ResultSetMapper<Entry> {
        @Override
        public Entry map(int index, ResultSet r, StatementContext ctx) throws SQLException {
            return new Entry(r.getInt("entry_number"), r.getString("sha256hex"));
        }
    }
}
