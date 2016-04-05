package uk.gov.register.presentation.dao;

import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;
import org.skife.jdbi.v2.sqlobject.customizers.SingleValueResult;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public interface EntryDAO {
    @SqlQuery("select * from entry where entry_number=:entry_number")
    @RegisterMapper(NewEntryMapper.class)
    @SingleValueResult(Entry.class)
    Optional<Entry> findByEntryNumber(@Bind("entry_number") int entryNumber);

    class NewEntryMapper implements ResultSetMapper<Entry> {
        @Override
        public Entry map(int index, ResultSet r, StatementContext ctx) throws SQLException {
            return new Entry(r.getString("entry_number"), r.getString("sha256hex"), r.getTimestamp("timestamp"));
        }
    }
}

