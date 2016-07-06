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
import uk.gov.indexer.dao.CurrentKey;
import uk.gov.indexer.dao.ExtendedDestinationDBUpdateDAO;
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
    @Rule
    public WireMockRule wireMockRule = new WireMockRule(8090);

    @Mock
    CloudwatchRecordsProcessedUpdater cloudwatchRecordsProcessedUpdater;

    @SuppressWarnings("ConstantConditions")
    @Test
    public void readEntriesFromMintPostgresDBAndWriteToReadApi() throws InterruptedException, SQLException {
        try (Connection mintConnection = createMintConnection(); Connection presentationConnection = createPresentationConnection()) {
            try (Statement mintStatement = mintConnection.createStatement(); Statement presentationStatement = presentationConnection.createStatement()) {
                DBI mintDbi = new DBI("jdbc:postgresql://localhost:5432/ft_openregister_java?user=postgres");
                DBI presentationDbi = new DBI("jdbc:postgresql://localhost:5432/ft_openregister_java_presentation?user=postgres");

                recreateMintTables(mintStatement, mintDbi);
                dropReadApiTables(presentationStatement);

                SourceDBQueryDAO sourceDBQueryDAO = mintDbi.open().attach(SourceDBQueryDAO.class);
                ExtendedDestinationDBUpdateDAO destinationDBUpdateDAO = presentationDbi.open().attach(ExtendedDestinationDBUpdateDAO.class);
                IndexerTask indexerTask = new IndexerTask(Optional.of(cloudwatchRecordsProcessedUpdater), "food-premises", sourceDBQueryDAO, destinationDBUpdateDAO);


                InOrder inOrder = Mockito.inOrder(cloudwatchRecordsProcessedUpdater);

                //load 1 entry and confirm the changes
                loadEntriesInMintDB(1, 0);

                runIndexerAndVerifyResult(presentationStatement, indexerTask, 1, 1);
                assertThat(currentKeys(presentationStatement).toString(), CoreMatchers.equalTo("[{1,fp_1}]"));
                assertNoOfRecordsAndEntries(presentationStatement, 1, 1);
                inOrder.verify(cloudwatchRecordsProcessedUpdater).update(1);

                //load 1 more entry and confirm the changes
                loadEntriesInMintDB(1, 1);

                runIndexerAndVerifyResult(presentationStatement, indexerTask, 2, 1);
                assertThat(currentKeys(presentationStatement).toString(), CoreMatchers.equalTo("[{2,fp_1}]"));
                assertNoOfRecordsAndEntries(presentationStatement, 2, 1);
                inOrder.verify(cloudwatchRecordsProcessedUpdater).update(1);

                //load 5 more entry and confirm the changes
                loadEntriesInMintDB(5, 2);

                runIndexerAndVerifyResult(presentationStatement, indexerTask, 7, 5);

                assertThat(currentKeys(presentationStatement).toString(), CoreMatchers.equalTo("[{7,fp_5}, {3,fp_1}, {4,fp_2}, {5,fp_3}, {6,fp_4}]"));
                assertNoOfRecordsAndEntries(presentationStatement, 7, 5);
                inOrder.verify(cloudwatchRecordsProcessedUpdater).update(5);

                //load 1 more entry and confirm the changes
                loadEntriesInMintDB(1, 7);

                runIndexerAndVerifyResult(presentationStatement, indexerTask, 8, 5);

                assertThat(currentKeys(presentationStatement).toString(), CoreMatchers.equalTo("[{7,fp_5}, {4,fp_2}, {5,fp_3}, {6,fp_4}, {8,fp_1}]"));
                assertNoOfRecordsAndEntries(presentationStatement, 8, 5);
                inOrder.verify(cloudwatchRecordsProcessedUpdater).update(1);

                //run indexer again when no new entries available, confirms that nothing changes but cloudwatch get notification with 0 entry
                runIndexerAndVerifyResult(presentationStatement, indexerTask, 8, 5);

                inOrder.verify(cloudwatchRecordsProcessedUpdater).update(0);
            }
        }
    }

    @Test
    public void confirmThatCurrentkeyTableLoadsOnlyLatestRecord() throws SQLException {
        try (Connection mintConnection = createMintConnection(); Connection presentationConnection = createPresentationConnection()) {
            try (Statement mintStatement = mintConnection.createStatement(); Statement presentationStatement = presentationConnection.createStatement()) {

                DBI mintDbi = new DBI("jdbc:postgresql://localhost:5432/ft_openregister_java?user=postgres");
                DBI presentationDbi = new DBI("jdbc:postgresql://localhost:5432/ft_openregister_java_presentation?user=postgres");

                recreateMintTables(mintStatement, mintDbi);
                dropReadApiTables(presentationStatement);

                SourceDBQueryDAO sourceDBQueryDAO = mintDbi.open().attach(SourceDBQueryDAO.class);
                ExtendedDestinationDBUpdateDAO destinationDBUpdateDAO = presentationDbi.open().attach(ExtendedDestinationDBUpdateDAO.class);
                IndexerTask indexerTask = new IndexerTask(Optional.of(cloudwatchRecordsProcessedUpdater), "food-premises", sourceDBQueryDAO, destinationDBUpdateDAO);

                loadEntriesInMintDB(2, 0);
                loadEntriesInMintDB(2, 2);

                indexerTask.run();

                assertThat(currentKeys(presentationStatement).toString(), CoreMatchers.equalTo("[{3,fp_1}, {4,fp_2}]"));
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

    private void runIndexerAndVerifyResult(Statement statement, IndexerTask indexerEntryItemTask, int expectedEntries, int expectedItems) throws SQLException {
        indexerEntryItemTask.run();

        try (ResultSet entries = statement.executeQuery("select count(*) from entry")) {
            entries.next();
            assertThat(entries.getInt("count"), CoreMatchers.equalTo(expectedEntries));
        }
        try (ResultSet items = statement.executeQuery("select count(*) from item")) {
            items.next();
            assertThat(items.getInt("count"), CoreMatchers.equalTo(expectedItems));
        }
    }

    private void loadEntriesInMintDB(int noOfEntries, int currentEntryNumber) throws SQLException {
        try (Statement statement = createMintConnection().createStatement()) {
            for (int entryNumber = 1; entryNumber <= noOfEntries; entryNumber++) {
                statement.execute(String.format("insert into entry(entry_number,sha256hex) values(%d,'hash%s')", currentEntryNumber + entryNumber, entryNumber));

                try (ResultSet resultSet = statement.executeQuery(String.format("select count(*) from item where sha256hex = 'hash%s'", entryNumber))) {
                    resultSet.next();
                    if (resultSet.getInt("count") == 0) {
                        statement.execute(String.format("insert into item(sha256hex, content) values('hash%s', '{\"food-premises\":\"fp_%s\",\"business\":\"company:123\"}')", entryNumber, entryNumber));
                    }
                }
            }
        }
    }

    private void recreateMintTables(Statement statement, DBI mintDbi) throws SQLException {
        statement.execute("drop table if exists entry");
        statement.execute("drop table if exists item");

        mintDbi.open().attach(uk.gov.store.EntryDAO.class).ensureSchema();
        mintDbi.open().attach(uk.gov.store.ItemDAO.class).ensureSchema();
    }

    private void dropReadApiTables(Statement statement) throws SQLException {
        statement.execute("drop table if exists entry");
        statement.execute("drop table if exists item");
        statement.execute("drop table if exists current_keys");
        statement.execute("drop table if exists total_entries");
        statement.execute("drop table if exists total_records");
    }

    private Connection createMintConnection() throws SQLException {
        PGSimpleDataSource dataSource = new PGSimpleDataSource();
        dataSource.setDatabaseName("ft_openregister_java");
        dataSource.setUser("postgres");
        return dataSource.getConnection();
    }

    private Connection createPresentationConnection() throws SQLException {
        PGSimpleDataSource dataSource = new PGSimpleDataSource();
        dataSource.setDatabaseName("ft_openregister_java_presentation");
        dataSource.setUser("postgres");
        return dataSource.getConnection();
    }
}
