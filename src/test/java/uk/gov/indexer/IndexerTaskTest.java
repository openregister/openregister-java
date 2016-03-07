package uk.gov.indexer;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
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
import uk.gov.indexer.ctserver.CTServer;
import uk.gov.indexer.dao.CurrentKey;
import uk.gov.indexer.dao.DestinationDBUpdateDAO;
import uk.gov.indexer.dao.SourceDBQueryDAO;
import uk.gov.indexer.fetchers.CTDataSource;
import uk.gov.indexer.fetchers.DataSource;
import uk.gov.indexer.fetchers.PGDataSource;
import uk.gov.indexer.monitoring.CloudwatchRecordsProcessedUpdater;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
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
    public void readEntriesFromMintCTServerAndWriteToReadApi() throws InterruptedException, SQLException {
        stubFor(get(urlEqualTo("/ct/v1/get-sth"))
                .willReturn(aResponse()
                                .withStatus(200)
                                .withBody("{ " +
                                        "\"tree_size\": 5, " +
                                        "\"timestamp\": 1447421303202, " +
                                        "\"sha256_root_hash\": \"" + ROOT_HASH + "\"," +
                                        "\"tree_head_signature\": \"" + TREE_HEAD_SIGNATURE + "\" " +
                                        "}")
                ));

        stubFor(get(urlEqualTo("/ct/v1/get-entries?start=0&end=4"))
                .willReturn(aResponse()
                                .withStatus(200)
                                .withBody("{ \"entries\": " +
                                        "[ " +
                                        "{ \"leaf_input\": \"AAAAAAFSeasJ5IAAAABZeyAib3duZXIiOiAiRm9yZXN0cnkgQ29tbWlzc2lvbiIsICJlbmQtZGF0ZSI6ICIiLCAiZ292ZXJubWVudC1kb21haW4iOiAiN3N0YW5lcy5nb3YudWsiIH0AAA==\", \"extra_data\": \"\" }, " +
                                        "{ \"leaf_input\": \"AAAAAAFSeasJnoAAAABqeyAib3duZXIiOiAiNHAncyBQdWJsaWMgUHJpdmF0ZSBQYXJ0bmVyc2hpcHMgUHJvZ3JhbSIsICJlbmQtZGF0ZSI6ICIiLCAiZ292ZXJubWVudC1kb21haW4iOiAiNHBzLmdvdi51ayIgfQAA\", \"extra_data\": \"\" } " +
                                        "] " +
                                        "}")
                ));
        stubFor(get(urlEqualTo("/ct/v1/get-entries?start=2&end=4"))
                .willReturn(aResponse()
                                .withStatus(200)
                                .withBody("{ \"entries\": " +
                                        "[ " +
                                        "{ \"leaf_input\": \"AAAAAAFSeasMBIAAAABheyAib3duZXIiOiAiQWJlcmRlZW5zaGlyZSBDb3VuY2lsIiwgImVuZC1kYXRlIjogIiIsICJnb3Zlcm5tZW50LWRvbWFpbiI6ICJhYmVyZGVlbnNoaXJlLmdvdi51ayIgfQAA\", \"extra_data\": \"\" }, " +
                                        "{ \"leaf_input\": \"AAAAAAFSeasMWIAAAABteyAib3duZXIiOiAiQWJlcmdhdmVubnkgVG93biBDb3VuY2lsIiwgImVuZC1kYXRlIjogIiIsICJnb3Zlcm5tZW50LWRvbWFpbiI6ICJBYmVyZ2F2ZW5ueVRvd25Db3VuY2lsLmdvdi51ayIgfQAA\", \"extra_data\": \"\" } " +
                                        "] " +
                                        "}")
                ));
        stubFor(get(urlEqualTo("/ct/v1/get-entries?start=4&end=4"))
                .willReturn(aResponse()
                                .withStatus(200)
                                .withBody("{ \"entries\": " +
                                        "[ " +
                                        "{ \"leaf_input\": \"AAAAAAFSeasIN4AAAABdeyAib3duZXIiOiAiVmFsZSBvZiBHbGFtb3JnYW4gQ291bmNpbCIsICJlbmQtZGF0ZSI6ICIiLCAiZ292ZXJubWVudC1kb21haW4iOiAiMXZhbGUuZ292LnVrIiB9AAA=\", \"extra_data\": \"\" } " +
                                        "] " +
                                        "}")
                ));


        try (Connection connection = createConnection()) {
            try (Statement statement = connection.createStatement()) {
                dropReadApiTables(statement);

                DataSource dataSource = new CTDataSource(new CTServer("http://localhost:8090"));

                DBI destDbi = new DBI("jdbc:postgresql://localhost:5432/test_indexer?user=postgres");

                DestinationDBUpdateDAO destinationDBUpdateDAO = destDbi.open().attach(DestinationDBUpdateDAO.class);

                IndexerTask indexerTask = new IndexerTask(Optional.of(cloudwatchRecordsProcessedUpdater), "government-domain", dataSource, destinationDBUpdateDAO);
                InOrder inOrder = Mockito.inOrder(cloudwatchRecordsProcessedUpdater);

                runIndexerAndVerifyResult(statement, indexerTask, 5);

                inOrder.verify(cloudwatchRecordsProcessedUpdater).update(5);

                //run indexer again when no new entries available, confirms that nothing changes but cloudwatch get notification with 0 entry
                indexerTask.run();

                inOrder.verify(cloudwatchRecordsProcessedUpdater).update(0);
            }
        }
    }

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
                IndexerTask indexerTask = new IndexerTask(Optional.of(cloudwatchRecordsProcessedUpdater), "food-premises", new PGDataSource(sourceDBQueryDAO), destinationDBUpdateDAO);

                InOrder inOrder = Mockito.inOrder(cloudwatchRecordsProcessedUpdater);

                //load 1 entry and confirm the changes
                loadEntriesInMintDB(1);

                runIndexerAndVerifyResult(statement, indexerTask, 1);
                assertThat(currentKeys(statement).toString(), CoreMatchers.equalTo("[{1,fp_1}]"));
                assertNoOfRecordsAndEntries(statement, 1, 1);

                inOrder.verify(cloudwatchRecordsProcessedUpdater).update(1);

                //load 1 more entry and confirm the changes
                loadEntriesInMintDB(1);

                runIndexerAndVerifyResult(statement, indexerTask, 2);
                assertThat(currentKeys(statement).toString(), CoreMatchers.equalTo("[{2,fp_1}]"));
                assertNoOfRecordsAndEntries(statement, 2, 1);

                inOrder.verify(cloudwatchRecordsProcessedUpdater).update(1);

                //load 5 more entry and confirm the changes
                loadEntriesInMintDB(5);

                runIndexerAndVerifyResult(statement, indexerTask, 7);
                assertThat(currentKeys(statement).toString(), CoreMatchers.equalTo("[{7,fp_5}, {3,fp_1}, {4,fp_2}, {5,fp_3}, {6,fp_4}]"));
                assertNoOfRecordsAndEntries(statement, 7, 5);

                inOrder.verify(cloudwatchRecordsProcessedUpdater).update(5);

                //load 1 more entry and confirm the changes
                loadEntriesInMintDB(1);

                runIndexerAndVerifyResult(statement, indexerTask, 8);
                assertThat(currentKeys(statement).toString(), CoreMatchers.equalTo("[{7,fp_5}, {4,fp_2}, {5,fp_3}, {6,fp_4}, {8,fp_1}]"));
                assertNoOfRecordsAndEntries(statement, 8, 5);

                inOrder.verify(cloudwatchRecordsProcessedUpdater).update(1);

                //run indexer again when no new entries available, confirms that nothing changes but cloudwatch get notification with 0 entry
                runIndexerAndVerifyResult(statement, indexerTask, 8);

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
                IndexerTask indexerTask = new IndexerTask(Optional.of(cloudwatchRecordsProcessedUpdater), "food-premises", new PGDataSource(sourceDBQueryDAO), destinationDBUpdateDAO);

                loadEntriesInMintDB(2);
                loadEntriesInMintDB(2);

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

    private void runIndexerAndVerifyResult(Statement statement, IndexerTask indexerTask, int expectedEntries) throws SQLException {
        indexerTask.run();

        verifyNumberOfEntriesInOrderedEntryIndexTable(statement, expectedEntries);

        verifySTH(statement, expectedEntries);
    }


    private void verifySTH(Statement statement, int expectedTreeSize) throws SQLException {
        try (ResultSet resultSet = statement.executeQuery("select * from sth")) {
            resultSet.next();
            assertThat(resultSet.getInt("tree_size"), CoreMatchers.equalTo(expectedTreeSize));
            assertThat(resultSet.getLong("timestamp"), CoreMatchers.equalTo(1447421303202L));
            assertThat(resultSet.getString("sha256_root_hash"), CoreMatchers.equalTo(ROOT_HASH));
            assertThat(resultSet.getString("tree_head_signature"), CoreMatchers.equalTo(TREE_HEAD_SIGNATURE));
        }
    }

    private void verifyNumberOfEntriesInOrderedEntryIndexTable(Statement statement, int expectedEntries) throws SQLException {
        try (ResultSet resultSet = statement.executeQuery("select count(*) from ordered_entry_index")) {
            resultSet.next();
            assertThat(resultSet.getInt("count"), CoreMatchers.equalTo(expectedEntries));
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
    }

    private void recreateEntriesTable(Statement statement) throws SQLException {
        statement.execute("drop table if exists entries");
        statement.execute("create table if not exists entries (id serial primary key, entry bytea)");
    }

    private void dropReadApiTables(Statement statement) throws SQLException {
        statement.execute("drop table if exists sth");
        statement.execute("drop table if exists ordered_entry_index");
        statement.execute("drop table if exists current_keys");
        statement.execute("drop table if exists total_entries");
        statement.execute("drop table if exists total_records");
    }

    private Connection createConnection() throws SQLException {
        PGSimpleDataSource dataSource = new PGSimpleDataSource();
        dataSource.setDatabaseName("test_indexer");
        dataSource.setUser("postgres");
        return dataSource.getConnection();
    }
}
