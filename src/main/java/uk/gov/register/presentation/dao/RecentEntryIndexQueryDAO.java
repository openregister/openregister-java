package uk.gov.register.presentation.dao;

import com.google.common.base.Optional;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;
import org.skife.jdbi.v2.sqlobject.customizers.SingleValueResult;
import uk.gov.register.presentation.DbRecord;
import uk.gov.register.presentation.mapper.RecordMapper;

import java.util.List;

@RegisterMapper(RecordMapper.class)
public interface RecentEntryIndexQueryDAO {

    @SqlQuery("SELECT entry FROM ordered_entry_index ORDER BY id DESC LIMIT :limit")
    List<DbRecord> getFeeds(@Bind("limit") int maxNumberToFetch);

    @SqlQuery("SELECT entry FROM ordered_entry_index WHERE (entry #>> ARRAY['entry',:key]) = :value ORDER BY id DESC limit 1")
    @SingleValueResult(DbRecord.class)
    Optional<DbRecord> findByKeyValue(@Bind("key") String key, @Bind("value") String value);

    @SqlQuery("SELECT entry FROM ordered_entry_index WHERE (entry #>> ARRAY['entry',:key]) = :value ORDER BY id DESC")
    List<DbRecord> findAllByKeyValue(@Bind("key") String key, @Bind("value") String value);

    @SqlQuery("SELECT entry FROM ordered_entry_index WHERE (entry #>> ARRAY['hash']) = :hash")
    @SingleValueResult(DbRecord.class)
    Optional<DbRecord> findByHash(@Bind("hash") String hash);

    @SqlQuery("SELECT i.id, i.entry FROM ordered_entry_index i, ( " +
            "SELECT MAX(id) AS id " +
            "FROM ordered_entry_index " +
            "GROUP BY (entry #>> ARRAY['entry',:key])) AS ii " +
            "WHERE i.id = ii.id " +
            "ORDER BY (entry #>> ARRAY['entry',:key]) DESC LIMIT :limit")
    List<DbRecord> getAllRecords(@Bind("key") String name,
                               @Bind("limit") int maxNumberToFetch);
}
