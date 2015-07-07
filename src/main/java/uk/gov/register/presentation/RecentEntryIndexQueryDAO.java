package uk.gov.register.presentation;

import com.fasterxml.jackson.databind.JsonNode;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;

import java.util.List;

public interface RecentEntryIndexQueryDAO {
    @RegisterMapper(JsonNodeMapper.class)
    @SqlQuery("SELECT entry FROM ordered_entry_index ORDER BY id DESC LIMIT :limit")
    List<JsonNode> getLatestEntries(@Bind("limit") int maxNumberToFetch);
}
