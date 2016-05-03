package uk.gov.indexer.dao;

import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;
import org.skife.jdbi.v2.sqlobject.stringtemplate.UseStringTemplate3StatementLocator;

import java.util.List;

@UseStringTemplate3StatementLocator
public interface SourceDBQueryDAO extends DBConnectionDAO {
    @RegisterMapper(RecordMapper.class)
    @SqlQuery("SELECT entry_number, entry.sha256hex as sha256hex, timestamp, content FROM item, entry WHERE item.sha256hex=entry.sha256hex and entry_number > :lastReadEntryNumber ORDER BY entry_number LIMIT 5000")
    List<Record> readRecords(@Bind("lastReadEntryNumber") int lastReadEntryNumber);

}
