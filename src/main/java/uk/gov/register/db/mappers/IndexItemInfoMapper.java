package uk.gov.register.db.mappers;

import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;
import uk.gov.register.indexer.IndexEntryNumberItemCountPair;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class IndexItemInfoMapper implements ResultSetMapper<IndexEntryNumberItemCountPair> {
	@Override
	public IndexEntryNumberItemCountPair map(int index, ResultSet r, StatementContext ctx) throws SQLException {
		Optional<Integer> startIndexEntryNumber = (r.getInt("start_index_entry_number") > 0)
				? Optional.ofNullable(r.getInt("start_index_entry_number"))
				: Optional.empty();
		int existingItemCount = r.getInt("existing_item_count");

		return new IndexEntryNumberItemCountPair(startIndexEntryNumber, existingItemCount);
	}
}
