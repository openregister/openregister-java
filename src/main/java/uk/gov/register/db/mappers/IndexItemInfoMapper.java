package uk.gov.register.db.mappers;

import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;
import uk.gov.register.indexer.IntegerItemPair;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class IndexItemInfoMapper implements ResultSetMapper<IntegerItemPair> {
	@Override
	public IntegerItemPair map(int index, ResultSet r, StatementContext ctx) throws SQLException {
		Optional<Integer> startIndexEntryNumber = Optional.ofNullable(r.getInt("start_index_entry_number"));
		int existingItemCount = r.getInt("existing_item_count");

		return new IntegerItemPair(startIndexEntryNumber, existingItemCount);
	}
}
