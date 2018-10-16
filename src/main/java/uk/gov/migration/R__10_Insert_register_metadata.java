package uk.gov.migration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Streams;
import io.dropwizard.jackson.Jackson;
import org.flywaydb.core.api.configuration.FlywayConfiguration;
import org.flywaydb.core.api.migration.MigrationChecksumProvider;
import org.flywaydb.core.api.migration.jdbc.BaseJdbcMigration;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;
import uk.gov.register.configuration.IndexFunctionConfiguration.IndexNames;
import uk.gov.register.core.Entry;
import uk.gov.register.core.EntryType;
import uk.gov.register.core.Blob;
import uk.gov.register.core.Record;
import uk.gov.register.db.*;
import uk.gov.register.util.EntryItemPair;
import uk.gov.register.util.HashValue;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.stream.Collectors.toList;

public class R__10_Insert_register_metadata extends BaseJdbcMigration implements MigrationChecksumProvider {
	private static final ObjectMapper YAML_MAPPER = Jackson.newObjectMapper(new YAMLFactory());
	private static final ObjectMapper JSON_MAPPER = Jackson.newObjectMapper();

	private FlywayConfiguration flywayConfiguration;

	@Override
	public void migrate(Connection connection) throws Exception {
		InputStream registersRecordsInputStream = null;
		InputStream fieldsRecordsInputStream = null;
		
		try {
			TempDataSource ds = new TempDataSource(connection);
			DBI dbi = new DBI(ds);
			Handle handle = dbi.open();

			EntryQueryDAO entryQueryDAO = handle.attach(EntryQueryDAO.class);
			if (entryQueryDAO.getTotalEntries(connection.getSchema()) > 0) {
				List<Record> records = new ArrayList<>();

				String registerName = flywayConfiguration.getPlaceholders().get("registerName");
				Record nameRecord = singleRecord("name", registerName, 1);
				records.add(nameRecord);

				String custodianName = flywayConfiguration.getPlaceholders().get("custodianName");
				if (!custodianName.isEmpty()) {
					Record custodianRecord = singleRecord("custodian", custodianName, 2);
					records.add(custodianRecord);
				}

				registersRecordsInputStream = getInputStream(flywayConfiguration.getPlaceholders().get("registersYamlUrl"));

				Record registerRecord = parseYamlToRecords(Arrays.asList(registerName), registersRecordsInputStream, "register").get(0);
				Blob registerItem = registerRecord.getBlobs().get(0);
				List<String> fieldNames = getFieldNames(registerItem);

				fieldsRecordsInputStream = getInputStream(flywayConfiguration.getPlaceholders().get("fieldsYamlUrl"));
				records.addAll(parseYamlToRecords(fieldNames, fieldsRecordsInputStream, "field"));

				records.add(registerRecord);

				AtomicInteger entryNumber = new AtomicInteger(1);

				List<Record> allRecordsFixedEntryNumbers = records.stream()
						.map(r -> copyWithEntryNumber(r, entryNumber.getAndIncrement())).collect(toList());

				writeRecords(connection, handle, allRecordsFixedEntryNumbers);
			}
		} finally {
			if (registersRecordsInputStream != null) {
				registersRecordsInputStream.close();
			}
			if (fieldsRecordsInputStream != null) {
				fieldsRecordsInputStream.close();
			}
		}
	}

	private Record singleRecord(String name, String value, int entryNumber) {
		Map<String, String> registerNameMap = ImmutableMap.of(name, value);
		Blob nameItem = new Blob(JSON_MAPPER.convertValue(registerNameMap, JsonNode.class));
		Entry nameEntry = new Entry(entryNumber, nameItem.getSha256hex(), Instant.now(), name, EntryType.system);
		return new Record(nameEntry, nameItem);

	}

	InputStream getInputStream(String url) throws IOException {
		URL registersYamlUrl = new URL(url);
		URLConnection registersUrlConnection = registersYamlUrl.openConnection();
		registersUrlConnection.setConnectTimeout(5000);
		registersUrlConnection.setReadTimeout(5000);
		return registersUrlConnection.getInputStream();
	}

	List<Record> parseYamlToRecords(List<String> keys, InputStream stream, String prefix) throws IOException {
		final JsonNode jsonNode = YAML_MAPPER.readTree(stream);
		List<Record> records = new ArrayList<>();
		for (String key : keys) {
			JsonNode entryNode = jsonNode.get(key);
			ArrayNode itemNodes = (ArrayNode) entryNode.get("item");
			Blob item = new Blob(itemNodes.get(0));
			Entry entry = JSON_MAPPER.treeToValue(entryNode, Entry.class);
			Entry entryNewKey = partialCopy(entry, prefix + ":" + entry.getKey(), item.getSha256hex());
			records.add(new Record(entryNewKey, item));
		}
		return records;
	}

	private List<String> getFieldNames(Blob registerItem) {
		ArrayNode fieldNamesArray = (ArrayNode) registerItem.getContent().get("fields");
		List<String> fieldNames = new ArrayList<>();
		for (JsonNode jsonNode : fieldNamesArray) {
			fieldNames.add(jsonNode.textValue());
		}
		return fieldNames;
	}

	private Entry partialCopy(Entry entry, String key, HashValue hashValue) {
		return new Entry(entry.getIndexEntryNumber(), entry.getEntryNumber(), Arrays.asList(hashValue), entry.getTimestamp(), key);
	}

	private Record copyWithEntryNumber(Record record, int entryNumber) {
		Entry entry = record.getEntry();
		Entry newEntry = new Entry(entryNumber, entryNumber, entry.getItemHashes(), entry.getTimestamp(), entry.getKey(), entry.getEntryType());
		return new Record(newEntry, record.getBlobs());
	}

	private void writeRecords(Connection connection, Handle handle, Iterable<Record> records) throws SQLException {
		List<Blob> items = Streams.stream(records).map(r -> r.getBlobs().get(0)).collect(toList());
		List<Entry> entries = Streams.stream(records).map(r -> r.getEntry()).collect(toList());
		List<EntryItemPair> entryItems = entries.stream()
				.map(e -> new EntryItemPair(e.getEntryNumber(), e.getItemHashes().get(0))).collect(toList());


		ItemDAO itemDAO = handle.attach(ItemDAO.class);
		EntryDAO entryDAO = handle.attach(EntryDAO.class);
		EntryBlobDAO entryBlobDAO = handle.attach(EntryBlobDAO.class);
		IndexDAO indexDAO = handle.attach(IndexDAO.class);

		itemDAO.insertInBatch(items, connection.getSchema());
		entryDAO.insertInBatch(entries, connection.getSchema(), "entry_system");
		entryBlobDAO.insertInBatch(entryItems, connection.getSchema(), "entry_item_system");

		records.forEach(r -> {
			try {
				Entry entry = r.getEntry();
				indexDAO.start(IndexNames.METADATA, entry.getKey(), entry.getItemHashes().get(0).getValue(),
						entry.getEntryNumber(), entry.getIndexEntryNumber(), connection.getSchema());
			} catch (SQLException e) {
				e.printStackTrace();
			}
		});

		// Flyway should close the connection so we don't close the handle here
	}

	@Override
	public void setFlywayConfiguration(FlywayConfiguration flywayConfiguration) {
		this.flywayConfiguration = flywayConfiguration;
	}

	@Override
	public Integer getChecksum() {
		return 1;
	}
}
