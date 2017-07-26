package uk.gov.migration;

import io.dropwizard.jdbi.OptionalContainerFactory;
import org.flywaydb.core.api.migration.MigrationChecksumProvider;
import org.flywaydb.core.api.migration.jdbc.BaseJdbcMigration;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;
import uk.gov.register.configuration.IndexFunctionConfiguration;
import uk.gov.register.core.*;
import uk.gov.register.db.DerivationRecordIndex;
import uk.gov.register.db.EntryQueryDAO;
import uk.gov.register.db.IndexDAO;
import uk.gov.register.db.IndexQueryDAO;
import uk.gov.register.indexer.IndexDriver;
import uk.gov.register.indexer.function.RecordIndexFunction;
import uk.gov.register.store.DataAccessLayer;
import uk.gov.register.store.postgres.PostgresDataAccessLayer;
import uk.gov.register.util.HashValue;
import uk.gov.register.views.ConsistencyProof;
import uk.gov.register.views.EntryProof;
import uk.gov.register.views.RegisterProof;

import java.sql.Connection;
import java.time.Instant;
import java.util.*;

public class R__11_Migrate_current_keys_to_index extends BaseJdbcMigration implements MigrationChecksumProvider {
	@Override
	public void migrate(Connection connection) throws Exception {
		TempDataSource ds = new TempDataSource(connection);
		DBI dbi = new DBI(ds);
		dbi.registerContainerFactory(new OptionalContainerFactory());
		Handle handle = dbi.open();
		
		DataAccessLayer dataAccessLayer = new PostgresDataAccessLayer(
				handle.attach(EntryQueryDAO.class),
				handle.attach(IndexDAO.class),
				handle.attach(IndexQueryDAO.class),
				null,
				null,
				null,
				null,
				connection.getSchema());

		RegisterReadOnly register = new RecordIndexRegister(dataAccessLayer);
		IndexDriver indexDriver = new IndexDriver(dataAccessLayer);

		dataAccessLayer.getEntryIterator().forEachRemaining(entry -> {
			indexDriver.indexEntry(register, entry, new RecordIndexFunction(IndexFunctionConfiguration.IndexNames.RECORDS));
		});
	}
	
	@Override
	public Integer getChecksum() {
		return 2;
	}

	private class RecordIndexRegister implements RegisterReadOnly {
		private final DerivationRecordIndex derivationRecordIndex;
		
		public RecordIndexRegister(DataAccessLayer dataAccessLayer) {
			this.derivationRecordIndex = new DerivationRecordIndex(dataAccessLayer);
		}
		
		@Override
		public Optional<Item> getItemBySha256(HashValue hash) {
			return null;
		}

		@Override
		public Collection<Item> getAllItems() {
			return null;
		}

		@Override
		public Optional<Entry> getEntry(int entryNumber) {
			return null;
		}

		@Override
		public Collection<Entry> getEntries(int start, int limit) {
			return null;
		}

		@Override
		public Collection<Entry> getAllEntries() {
			return null;
		}

		@Override
		public int getTotalEntries() {
			return 0;
		}

		@Override
		public int getTotalEntries(EntryType entryType) {
			return 0;
		}

		@Override
		public Optional<Instant> getLastUpdatedTime() {
			return null;
		}

		@Override
		public Optional<Record> getRecord(String key) {
			return derivationRecordIndex.getRecord(key, IndexFunctionConfiguration.IndexNames.RECORDS);
		}

		@Override
		public int getTotalRecords() {
			return 0;
		}

		@Override
		public Collection<Entry> allEntriesOfRecord(String key) {
			return null;
		}

		@Override
		public List<Record> getRecords(int limit, int offset) {
			return null;
		}

		@Override
		public List<Record> max100RecordsFacetedByKeyValue(String key, String value) {
			return null;
		}

		@Override
		public RegisterProof getRegisterProof() {
			return null;
		}

		@Override
		public RegisterProof getRegisterProof(int entryNo) {
			return null;
		}

		@Override
		public EntryProof getEntryProof(int entryNumber, int totalEntries) {
			return null;
		}

		@Override
		public ConsistencyProof getConsistencyProof(int totalEntries1, int totalEntries2) {
			return null;
		}

		@Override
		public Iterator<Entry> getEntryIterator() {
			return null;
		}

		@Override
		public Iterator<Entry> getEntryIterator(int totalEntries1, int totalEntries2) {
			return null;
		}

		@Override
		public Iterator<Item> getItemIterator() {
			return null;
		}

		@Override
		public Iterator<Item> getItemIterator(int start, int end) {
			return null;
		}

		@Override
		public Iterator<Item> getSystemItemIterator() {
			return null;
		}

		@Override
		public Iterator<Entry> getDerivationEntryIterator(String indexName) {
			return null;
		}

		@Override
		public Iterator<Entry> getDerivationEntryIterator(String indexName, int totalEntries1, int totalEntries2) {
			return null;
		}

		@Override
		public RegisterName getRegisterName() {
			return null;
		}

		@Override
		public Optional<String> getCustodianName() {
			return null;
		}

		@Override
		public RegisterMetadata getRegisterMetadata() {
			return null;
		}

		@Override
		public Optional<Record> getDerivationRecord(String key, String derivationName) {
			return null;
		}

		@Override
		public List<Record> getDerivationRecords(int limit, int offset, String derivationName) {
			return null;
		}

		@Override
		public int getTotalDerivationRecords(String derivationName) {
			return 0;
		}

		@Override
		public Map<String, Field> getFieldsByName() {
			return null;
		}
	}
}
