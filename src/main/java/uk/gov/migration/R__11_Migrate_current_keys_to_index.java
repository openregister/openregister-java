package uk.gov.migration;

import com.google.common.collect.ImmutableMap;
import io.dropwizard.jdbi.OptionalContainerFactory;
import org.flywaydb.core.api.migration.MigrationChecksumProvider;
import org.flywaydb.core.api.migration.jdbc.BaseJdbcMigration;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;
import uk.gov.register.configuration.IndexFunctionConfiguration.IndexNames;
import uk.gov.register.core.Entry;
import uk.gov.register.core.EntryType;
import uk.gov.register.db.*;
import uk.gov.register.indexer.IndexDriver;
import uk.gov.register.indexer.function.LatestByKeyIndexFunction;
import uk.gov.register.store.postgres.PostgresDataAccessLayer;

import java.sql.Connection;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class R__11_Migrate_current_keys_to_index extends BaseJdbcMigration implements MigrationChecksumProvider {
	@Override
	public void migrate(Connection connection) throws Exception {
		TempDataSource ds = new TempDataSource(connection);
		DBI dbi = new DBI(ds);
		dbi.registerContainerFactory(new OptionalContainerFactory());
		Handle handle = dbi.open();
		
		PostgresDataAccessLayer dataAccessLayer = new PostgresDataAccessLayer(
				handle.attach(EntryQueryDAO.class),
				handle.attach(IndexDAO.class),
				handle.attach(IndexQueryDAO.class),
				handle.attach(EntryDAO.class),
				handle.attach(EntryBlobDAO.class),
				handle.attach(BlobQueryDAO.class),
				handle.attach(BlobDAO.class),
				connection.getSchema(),
				new IndexDriver(),
				ImmutableMap.of(EntryType.user, Arrays.asList(new LatestByKeyIndexFunction(IndexNames.RECORD))));

		IndexDriver indexDriver = new IndexDriver();

		Map<String, Entry> entries = new HashMap<>();
		int currentIndexEntryNumber = 0;

		dataAccessLayer.getEntryIterator(IndexNames.RECORD).forEachRemaining(entry -> {
			indexDriver.indexEntry(dataAccessLayer, entry, new LatestByKeyIndexFunction(IndexNames.RECORD), entries, currentIndexEntryNumber);
		});
		
		dataAccessLayer.checkpoint();
	}
	
	@Override
	public Integer getChecksum() {
		return 1;
	}
}
