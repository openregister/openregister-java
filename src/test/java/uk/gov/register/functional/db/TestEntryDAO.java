package uk.gov.register.functional.db;

import org.skife.jdbi.v2.ResultIterator;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.Define;
import org.skife.jdbi.v2.sqlobject.customizers.FetchSize;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;
import org.skife.jdbi.v2.sqlobject.stringtemplate.UseStringTemplate3StatementLocator;
import uk.gov.register.core.Entry;
import uk.gov.register.db.mappers.EntryMapper;

import java.util.List;

@UseStringTemplate3StatementLocator
public interface TestEntryDAO {
    @SqlUpdate("delete from \"<schema>\".entry;" +
            "delete from \"<schema>\".entry_system;" +
            "delete from \"<schema>\".current_entry_number;" +
            "insert into \"<schema>\".current_entry_number values(0);")
    void wipeData(@Define("schema") String schema);

    @RegisterMapper(EntryMapper.class)
    @SqlQuery("select * from \"<schema>\".entry order by entry_number")
    List<Entry> getAllEntries(@Define("schema") String schema);

    @RegisterMapper(EntryMapper.class)
    @SqlQuery("select * from \"<schema>\".entry_system order by entry_number")
    List<Entry> getAllSystemEntries(@Define("schema") String schema);

    @SqlQuery("select * from \"<schema>\".entry where entry_number >= :entryNumber order by entry_number")
    @RegisterMapper(EntryMapper.class)
    @FetchSize(262144) // Has to be non-zero to enable cursor mode https://jdbc.postgresql.org/documentation/head/query.html#query-with-cursor
    ResultIterator<Entry> entriesIteratorFrom(@Bind("entryNumber") int entryNumber, @Define("schema") String schema);
}
