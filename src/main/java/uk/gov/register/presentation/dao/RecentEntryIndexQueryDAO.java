package uk.gov.register.presentation.dao;

import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;
import uk.gov.register.presentation.DbEntry;
import uk.gov.register.presentation.mapper.EntryMapper;

import java.util.List;

@RegisterMapper(EntryMapper.class)
public abstract class RecentEntryIndexQueryDAO {
    // TODO: This will be okay for small numbers of records
    @SqlQuery("SELECT serial_number,entry FROM ordered_entry_index ORDER BY serial_number DESC")
    public abstract List<DbEntry> getAllEntriesNoPagination();

    @SqlQuery("SELECT serial_number,entry FROM ordered_entry_index " +
            "WHERE serial_number IN(" +
            "SELECT serial_number FROM current_keys ORDER BY serial_number DESC LIMIT :limit OFFSET :offset" +
            ") ORDER BY serial_number DESC")
    public abstract List<DbEntry> getLatestEntriesOfRecords(@Bind("limit") long maxNumberToFetch, @Bind("offset") long offset);

}
