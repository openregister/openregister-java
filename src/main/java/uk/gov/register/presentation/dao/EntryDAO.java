package uk.gov.register.presentation.dao;

import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;
import org.skife.jdbi.v2.sqlobject.customizers.SingleValueResult;

import java.util.List;
import java.util.Optional;

public interface EntryDAO {
    @SqlQuery("select * from entry where entry_number=:entry_number")
    @RegisterMapper(NewEntryMapper.class)
    @SingleValueResult(Entry.class)
    Optional<Entry> findByEntryNumber(@Bind("entry_number") int entryNumber);

    @SqlQuery("select * from entry ORDER BY entry_number desc limit :limit offset :offset")
    @RegisterMapper(NewEntryMapper.class)
    List<Entry> getEntries(@Bind("limit") long maxNumberToFetch, @Bind("offset") long offset);
}

