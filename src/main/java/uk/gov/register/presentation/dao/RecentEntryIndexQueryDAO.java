package uk.gov.register.presentation.dao;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Optional;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;
import org.skife.jdbi.v2.sqlobject.customizers.SingleValueResult;
import uk.gov.register.presentation.mapper.JsonNodeMapper;

import java.util.List;

@RegisterMapper(JsonNodeMapper.class)
public interface RecentEntryIndexQueryDAO {

    @SqlQuery("SELECT entry FROM ordered_entry_index ORDER BY id DESC LIMIT :limit")
    List<JsonNode> getFeeds(@Bind("limit") int maxNumberToFetch);

    @SqlQuery("SELECT entry FROM ordered_entry_index WHERE (entry #>> ARRAY['entry',:key]) = :value ORDER BY id DESC limit 1")
    @SingleValueResult(JsonNode.class)
    Optional<JsonNode> findByKeyValue(@Bind("key") String key, @Bind("value") String value);

    @SqlQuery("SELECT entry FROM ordered_entry_index WHERE (entry #>> ARRAY['hash']) = :hash")
    @SingleValueResult(JsonNode.class)
    Optional<JsonNode> findByHash(@Bind("hash") String hash);
}
