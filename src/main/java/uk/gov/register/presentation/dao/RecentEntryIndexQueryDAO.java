package uk.gov.register.presentation.dao;

import com.fasterxml.jackson.databind.JsonNode;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.customizers.Define;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;
import uk.gov.register.presentation.mapper.JsonNodeMapper;

import java.util.List;

@RegisterMapper(JsonNodeMapper.class)
public interface RecentEntryIndexQueryDAO {

    @SqlQuery("SELECT entry FROM ordered_entry_index ORDER BY id DESC LIMIT :limit")
    List<JsonNode> getLatestEntries(@Bind("limit") int maxNumberToFetch);

    @SqlQuery("SELECT entry FROM ordered_entry_index WHERE entry @> '{\"ft_test_pkey\":\"ft_test_pkey_value_1\"}'")
    List<JsonNode> find(@Define("key") String primaryKey, @Bind("value") String value);
}
