package uk.gov.register.db.mappers;

import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.skife.jdbi.v2.DBI;
import uk.gov.register.db.IndexQueryDAO;
import uk.gov.register.functional.app.MigrateDatabaseRule;
import uk.gov.register.functional.app.WipeDatabaseRule;
import uk.gov.register.indexer.IndexEntryNumberItemCountPair;

import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static uk.gov.register.functional.app.TestRegister.local_authority_eng;

public class IndexBlobInfoMapperTest {
	private final DBI dbi = new DBI(local_authority_eng.getDatabaseConnectionString("IndexBlobInfoMapperTest"));
	private final String schema = local_authority_eng.getSchema();

	@ClassRule
	public static MigrateDatabaseRule migrateDatabaseRule = new MigrateDatabaseRule(local_authority_eng);
	@Rule
	public WipeDatabaseRule wipeDatabaseRule = new WipeDatabaseRule(local_authority_eng);
	
	@Test
	public void map_returnsPairWithEmptyStartIndexEntryNumberAndZeroItemCount() {
		IndexEntryNumberItemCountPair result = dbi.withHandle(h -> h.attach(IndexQueryDAO.class).getStartIndexEntryNumberAndExistingItemCount("local-authority-by-type", "UA", "abc", schema));
		
		assertThat(result.getExistingItemCount(), equalTo(0));
		assertThat(result.getStartIndexEntryNumber(), equalTo(Optional.empty()));
	}
	
	@Test
	public void map_returnsPairWithStartIndexEntryNumberAndItemCount() {
		IndexEntryNumberItemCountPair result = dbi.withHandle(h -> {
			h.execute("insert into \"local-authority-eng\".index (name, key, sha256hex, start_entry_number, start_index_entry_number) values ('local-authority-by-type', 'UA', 'abc', 5, 5)");
			return h.attach(IndexQueryDAO.class).getStartIndexEntryNumberAndExistingItemCount("local-authority-by-type", "UA", "abc", schema);
		});

		assertThat(result.getExistingItemCount(), equalTo(1));
		assertThat(result.getStartIndexEntryNumber(), equalTo(Optional.of(5)));
	}
}
