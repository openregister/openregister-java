package uk.gov.register.presentation.dao;

import com.fasterxml.jackson.databind.JsonNode;
import org.postgresql.util.PGobject;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;
import uk.gov.register.presentation.mapper.JsonNodeMapper;

import java.util.List;

@RegisterMapper(JsonNodeMapper.class)
public interface RecentEntryIndexQueryDAO {

    @SqlQuery("SELECT entry FROM ordered_entry_index ORDER BY id DESC LIMIT :limit")
    List<JsonNode> getLatestEntries(@Bind("limit") int maxNumberToFetch);

    @SqlQuery("SELECT i.id, i.entry FROM ordered_entry_index i, ( " +
                "SELECT MAX(id) AS id " +
                "FROM ordered_entry_index " +
                "GROUP BY entry ->> :key) AS ii " +
                "WHERE i.id = ii.id " +
                "ORDER BY i.entry ->> :key DESC LIMIT :limit")
    List<JsonNode> getAllEntries(@Bind("key") String name,
                                 @Bind("limit") int maxNumberToFetch);

    @SqlQuery("SELECT entry FROM ordered_entry_index WHERE entry @> :searchEntryJson")
    List<JsonNode> find(@Bind("searchEntryJson") PGobject searchEntryObject);
}
