package uk.gov.register.db.mappers;

import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;
import uk.gov.register.core.StartIndex;

import java.sql.ResultSet;
import java.sql.SQLException;

public class StartIndexMapper implements ResultSetMapper<StartIndex> {
	@Override
	public StartIndex map(int index, ResultSet r, StatementContext ctx) throws SQLException {
		String indexName = r.getString("name");
		String key = r.getString("key");
		String sha256hex = r.getString("sha256hex");
		int startEntryNumber = r.getInt("start_entry_number");
		int startIndexEntryNumber = r.getInt("start_index_entry_number");
		
		return new StartIndex(indexName, key, sha256hex, startEntryNumber, startIndexEntryNumber);
	}
}
