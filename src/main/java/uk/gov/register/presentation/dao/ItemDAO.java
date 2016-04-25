package uk.gov.register.presentation.dao;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.jackson.Jackson;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;
import org.skife.jdbi.v2.sqlobject.customizers.SingleValueResult;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Optional;

public interface ItemDAO {

    @SqlQuery("select * from item where sha256hex=:sha256hex")
    @SingleValueResult(Item.class)
    @RegisterMapper(ItemMapper.class)
    Optional<Item> getItemBySHA256(@Bind("sha256hex") String sha256Hash);

    //Note: This is fine for small data registers like country
    @SqlQuery("select * from item")
    @RegisterMapper(ItemMapper.class)
    Collection<Item> getAllItemsNoPagination();

    class ItemMapper implements ResultSetMapper<Item> {

        private final ObjectMapper objectMapper;

        public ItemMapper() {
            objectMapper = Jackson.newObjectMapper();
        }

        @Override
        public Item map(int index, ResultSet r, StatementContext ctx) throws SQLException {
            try {
                return new Item(r.getString("sha256hex"), objectMapper.readValue(r.getString("content"), JsonNode.class));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
