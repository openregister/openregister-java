package uk.gov.indexer.dao;

import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;
import org.skife.jdbi.v2.tweak.ResultSetMapper;
import org.skife.jdbi.v2.unstable.BindIn;
import org.skife.jdbi.v2.sqlobject.stringtemplate.UseStringTemplate3StatementLocator;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@UseStringTemplate3StatementLocator
public interface SourceDBQueryDAO extends DBConnectionDAO {
    String ENTRIES_TABLE = "entries";
    String ENTRY_TABLE = "entry";
    String ITEM_TABLE = "item";

    @SqlQuery("SELECT 1 FROM pg_tables WHERE tablename = '" + ENTRIES_TABLE + "'")
    boolean entriesTableExists();

    @SqlQuery("SELECT 1 FROM pg_tables WHERE tablename = '" + ENTRY_TABLE + "'")
    boolean entryTableExists();

    @RegisterMapper(FatEntryMapper.class)
    @SqlQuery("SELECT ID,ENTRY FROM " + ENTRIES_TABLE + " WHERE ID > :lastReadSerialNumber ORDER BY ID LIMIT 5000")
    List<FatEntry> read(@Bind("lastReadSerialNumber") int lastReadSerialNumber);

    @RegisterMapper(EntryMapper.class)
    @SqlQuery("SELECT entry_number, sha256hex, timestamp FROM " + ENTRY_TABLE + " WHERE entry_number > :lastReadEntryNumber ORDER BY entry_number LIMIT 5000")
    List<Entry> readEntries(@Bind("lastReadEntryNumber") int lastReadEntryNumber);

    @RegisterMapper(ItemMapper.class)
    @SqlQuery("SELECT sha256hex, content FROM " + ITEM_TABLE + " WHERE sha256hex in (<itemhashes>)")
    List<Item> readItems(@BindIn("itemhashes") Iterable<String> itemHashes);

    class FatEntryMapper implements ResultSetMapper<FatEntry> {
        @Override
        public FatEntry map(int index, ResultSet r, StatementContext ctx) throws SQLException {
            return new FatEntry(
                    r.getInt("ID"),
                    r.getBytes("ENTRY")
            );
        }
    }

    class EntryMapper implements ResultSetMapper<Entry> {
        @Override
        public Entry map(int index, ResultSet r, StatementContext ctx) throws SQLException {
            return new Entry(
                    r.getInt("entry_number"),
                    r.getString("sha256hex"),
                    r.getTimestamp("timestamp")
            );
        }
    }

    class ItemMapper implements ResultSetMapper<Item> {
        @Override
        public Item map(int index, ResultSet r, StatementContext ctx) throws SQLException {
            return new Item(
                    r.getString("sha256hex"),
                    r.getBytes("content")
            );
        }
    }
}
