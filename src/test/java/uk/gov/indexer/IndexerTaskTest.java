package uk.gov.indexer;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.sun.glass.ui.EventLoop;
import org.hamcrest.CoreMatchers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.postgresql.ds.PGSimpleDataSource;
import org.skife.jdbi.v2.DBI;
import uk.gov.indexer.dao.CurrentKey;
import uk.gov.indexer.dao.DestinationDBUpdateDAO;
import uk.gov.indexer.dao.SourceDBQueryDAO;
import uk.gov.indexer.monitoring.CloudwatchRecordsProcessedUpdater;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class IndexerTaskTest {
    static final String ROOT_HASH = "JATHxRF5gczvNPP1S1WuhD8jSx2bl+WoTt8bIE3YKvU=";
    static final String TREE_HEAD_SIGNATURE = "BAMARzBFAiEAkKM3aRUBKhShdCyrGLdd8lYBV52FLrwqjHa5/YuzK7ECIFTlRmNuKLqbVQv0QS8nq0pAUwgbilKOR5piBAIC8LpS";

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(8090);

    @Mock
    CloudwatchRecordsProcessedUpdater cloudwatchRecordsProcessedUpdater;

    @SuppressWarnings("ConstantConditions")
    @Test
    public void readEntriesFromMintPostgresDBAndWriteToReadApi() throws InterruptedException, SQLException {
        try (Connection connection = createConnection()) {
            try (Statement statement = connection.createStatement()) {

                recreateEntriesTable(statement);
                dropReadApiTables(statement);

                DBI dbi = new DBI("jdbc:postgresql://localhost:5432/test_indexer?user=postgres");

                SourceDBQueryDAO sourceDBQueryDAO = dbi.open().attach(SourceDBQueryDAO.class);
                DestinationDBUpdateDAO destinationDBUpdateDAO = dbi.open().attach(DestinationDBUpdateDAO.class);
                IndexerTask indexerTask = new IndexerTask(Optional.of(cloudwatchRecordsProcessedUpdater), "food-premises", sourceDBQueryDAO, destinationDBUpdateDAO);

                InOrder inOrder = Mockito.inOrder(cloudwatchRecordsProcessedUpdater);

                //load 1 entry and confirm the changes
                loadEntriesInMintDB(1);

                runIndexerAndVerifyResult(statement, indexerTask, 1, 1);
                assertThat(currentKeys(statement).toString(), CoreMatchers.equalTo("[{1,fp_1}]"));
                assertNoOfRecordsAndEntries(statement, 1, 1);

                inOrder.verify(cloudwatchRecordsProcessedUpdater).update(1);

                //load 1 more entry and confirm the changes
                loadEntriesInMintDB(1);

                runIndexerAndVerifyResult(statement, indexerTask, 2, 1);
                assertThat(currentKeys(statement).toString(), CoreMatchers.equalTo("[{2,fp_1}]"));
                assertNoOfRecordsAndEntries(statement, 2, 1);

                inOrder.verify(cloudwatchRecordsProcessedUpdater).update(1);

                //load 5 more entry and confirm the changes
                loadEntriesInMintDB(5);

                runIndexerAndVerifyResult(statement, indexerTask, 7, 5);
                assertThat(currentKeys(statement).toString(), CoreMatchers.equalTo("[{7,fp_5}, {3,fp_1}, {4,fp_2}, {5,fp_3}, {6,fp_4}]"));
                assertNoOfRecordsAndEntries(statement, 7, 5);

                inOrder.verify(cloudwatchRecordsProcessedUpdater).update(5);

                //load 1 more entry and confirm the changes
                loadEntriesInMintDB(1);

                runIndexerAndVerifyResult(statement, indexerTask, 8, 5);
                assertThat(currentKeys(statement).toString(), CoreMatchers.equalTo("[{7,fp_5}, {4,fp_2}, {5,fp_3}, {6,fp_4}, {8,fp_1}]"));
                assertNoOfRecordsAndEntries(statement, 8, 5);

                inOrder.verify(cloudwatchRecordsProcessedUpdater).update(1);

                //run indexer again when no new entries available, confirms that nothing changes but cloudwatch get notification with 0 entry
                runIndexerAndVerifyResult(statement, indexerTask, 8, 5);

                inOrder.verify(cloudwatchRecordsProcessedUpdater).update(0);
            }
        }
    }

    @Test
    public void confirmThatCurrentkeyTableLoadsOnlyLatestRecord() throws SQLException {
        try (Connection connection = createConnection()) {
            try (Statement statement = connection.createStatement()) {

                recreateEntriesTable(statement);
                dropReadApiTables(statement);

                DBI dbi = new DBI("jdbc:postgresql://localhost:5432/test_indexer?user=postgres");

                SourceDBQueryDAO sourceDBQueryDAO = dbi.open().attach(SourceDBQueryDAO.class);
                DestinationDBUpdateDAO destinationDBUpdateDAO = dbi.open().attach(DestinationDBUpdateDAO.class);
                IndexerTask indexerTask = new IndexerTask(Optional.of(cloudwatchRecordsProcessedUpdater), "food-premises", sourceDBQueryDAO, destinationDBUpdateDAO);

                loadEntriesInMintDB(2);
                loadEntriesInMintDB(2);

                indexerTask.run();

                assertThat(currentKeys(statement).toString(), CoreMatchers.equalTo("[{3,fp_1}, {4,fp_2}]"));
            }
        }
    }

    @Test
    public void confirmThatCurrentkeyTableLoadsOnlyLatestRecordFromEntryAndItem() throws SQLException {
        try (Connection connection = createConnection()) {
            try (Statement statement = connection.createStatement()) {

                recreateEntriesTable(statement);
                dropReadApiTables(statement);

                DBI dbi = new DBI("jdbc:postgresql://localhost:5432/test_indexer?user=postgres");

                SourceDBQueryDAO sourceDBQueryDAO = dbi.open().attach(SourceDBQueryDAO.class);
                DestinationDBUpdateDAO destinationDBUpdateDAO = dbi.open().attach(DestinationDBUpdateDAO.class);
                IndexerTask indexerTask = new IndexerTask(Optional.of(cloudwatchRecordsProcessedUpdater), "food-premises", sourceDBQueryDAO, destinationDBUpdateDAO);

                loadEntriesAndItemsInMintDB(2);
                loadEntriesAndItemsInMintDB(2);

                indexerTask.run();

                assertThat(currentKeys(statement).toString(), CoreMatchers.equalTo("[{3,fp_1}, {4,fp_2}]"));
            }
        }
    }

    private List<CurrentKey> currentKeys(Statement statement) throws SQLException {
        List<CurrentKey> currentKeys = new ArrayList<>();
        try (ResultSet resultSet = statement.executeQuery("select * from current_keys")) {
            while (resultSet.next()) {
                currentKeys.add(new CurrentKey(resultSet.getString("key"), resultSet.getInt("serial_number")));
            }
        }
        return currentKeys;
    }

    private void runIndexerAndVerifyResult(Statement statement, IndexerTask indexerTask, int expectedEntries, int expectedItems) throws SQLException {
        indexerTask.run();

        verifyNumberOfEntriesInOrderedEntryIndexTable(statement, expectedEntries);
        verifyNumberInEntryAndItemTables(statement, expectedEntries, expectedItems);
    }


    private void verifyNumberOfEntriesInOrderedEntryIndexTable(Statement statement, int expectedEntries) throws SQLException {
        try (ResultSet resultSet = statement.executeQuery("select count(*) from ordered_entry_index")) {
            resultSet.next();
            assertThat(resultSet.getInt("count"), CoreMatchers.equalTo(expectedEntries));
        }
    }

    private void verifyNumberInEntryAndItemTables(Statement statement, int expectedEntries, int expectedItems) throws SQLException {
        try (ResultSet entries = statement.executeQuery("select count(*) from entry")) {
            entries.next();
            assertThat(entries.getInt("count"), CoreMatchers.equalTo(expectedEntries));
        }
        try (ResultSet items = statement.executeQuery("select count(*) from item")) {
            items.next();
            assertThat(items.getInt("count"), CoreMatchers.equalTo(expectedItems));
        }
    }

    private void assertNoOfRecordsAndEntries(Statement statement, int entries, int records) throws SQLException {
        try (ResultSet resultSet = statement.executeQuery("select count from total_entries")) {
            resultSet.next();
            assertThat(resultSet.getInt("count"), CoreMatchers.equalTo(entries));
        }

        try (ResultSet resultSet = statement.executeQuery("select count from total_records")) {
            resultSet.next();
            assertThat(resultSet.getInt("count"), CoreMatchers.equalTo(records));
        }
    }

    private void loadEntriesInMintDB(int noOfEntries) throws SQLException {
        try (Statement statement = createConnection().createStatement()) {
            for (int entryNumber = 1; entryNumber <= noOfEntries; entryNumber++) {
                statement.execute(String.format("insert into entries(entry) values('{\"hash\": \"hash%s\", \"entry\": {\"food-premises\":\"fp_%s\",\"business\":\"company:123\"}}')", entryNumber, entryNumber));
            }
        }

        loadEntriesAndItemsInMintDB(noOfEntries);
    }

    private void loadEntriesAndItemsInMintDB(int noOfEntries) throws SQLException {
        try (Statement statement = createConnection().createStatement()) {
            for (int entryNumber = 1; entryNumber <= noOfEntries; entryNumber++) {
                statement.execute(String.format("insert into entry(sha256hex) values('hash%s')", entryNumber));

                try (ResultSet resultSet = statement.executeQuery(String.format("select count(*) from item where sha256hex = 'hash%s'", entryNumber))) {
                    resultSet.next();
                    if (resultSet.getInt("count") == 0) {
                        statement.execute(String.format("insert into item(sha256hex, content) values('hash%s', '{\"food-premises\":\"fp_%s\",\"business\":\"company:123\"}')", entryNumber, entryNumber));
                    }
                }
            }
        }
    }

    private void recreateEntriesTable(Statement statement) throws SQLException {
        statement.execute("drop table if exists entries");
        statement.execute("create table if not exists entries (id serial primary key, entry bytea)");

        recreateEntryAndItemTables(statement);
    }

    private void recreateEntryAndItemTables(Statement statement) throws SQLException {
        statement.execute("drop table if exists entry");
        statement.execute("drop table if exists item");
        statement.execute("create table if not exists entry (entry_number serial primary key, sha256hex varchar, timestamp timestamp default now())");
        statement.execute("create table if not exists item (sha256hex varchar primary key, content bytea)");
    }

    private void dropReadApiTables(Statement statement) throws SQLException {
        statement.execute("drop table if exists sth");
        statement.execute("drop table if exists ordered_entry_index");
        statement.execute("drop table if exists current_keys");
        statement.execute("drop table if exists total_entries");
        statement.execute("drop table if exists total_records");
        statement.execute("drop table if exists indexed_entry");
        statement.execute("drop table if exists indexed_item");
    }

    private Connection createConnection() throws SQLException {
        PGSimpleDataSource dataSource = new PGSimpleDataSource();
        dataSource.setDatabaseName("test_indexer");
        dataSource.setUser("postgres");
        return dataSource.getConnection();
    }
}
