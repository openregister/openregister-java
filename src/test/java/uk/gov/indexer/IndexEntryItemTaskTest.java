package uk.gov.indexer;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.hamcrest.CoreMatchers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.postgresql.ds.PGSimpleDataSource;
import org.skife.jdbi.v2.DBI;
import uk.gov.indexer.dao.DestinationDBUpdateDAO;
import uk.gov.indexer.dao.SourceDBQueryDAO;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class IndexEntryItemTaskTest {
    @Rule
    public WireMockRule wireMockRule = new WireMockRule(8090);

    @SuppressWarnings("ConstantConditions")
    @Test
    public void readEntriesFromMintPostgresDBAndWriteToReadApi() throws InterruptedException, SQLException {
        try (Connection mintConnection = createMintConnection(); Connection presentationConnection = createPresentationConnection()) {
            try (Statement mintStatement = mintConnection.createStatement(); Statement presentationStatement = presentationConnection.createStatement()) {

                recreateEntryAndItemTables(mintStatement);
                dropReadApiTables(presentationStatement);

                DBI mintDbi = new DBI("jdbc:postgresql://localhost:5432/test_indexer_mint?user=postgres");
                DBI presentationDbi = new DBI("jdbc:postgresql://localhost:5432/test_indexer_presentation?user=postgres");

                SourceDBQueryDAO sourceDBQueryDAO = mintDbi.open().attach(SourceDBQueryDAO.class);
                DestinationDBUpdateDAO destinationDBUpdateDAO = presentationDbi.open().attach(DestinationDBUpdateDAO.class);
                IndexEntryItemTask indexEntryItemTask = new IndexEntryItemTask("food-premises", sourceDBQueryDAO, destinationDBUpdateDAO);

                //load 1 entry and confirm the changes
                loadEntriesInMintDB(1);

                runIndexerAndVerifyResult(presentationStatement, indexEntryItemTask, 1, 1);

                //load 1 more entry and confirm the changes
                loadEntriesInMintDB(1);

                runIndexerAndVerifyResult(presentationStatement, indexEntryItemTask, 2, 1);

                //load 5 more entry and confirm the changes
                loadEntriesInMintDB(5);

                runIndexerAndVerifyResult(presentationStatement, indexEntryItemTask, 7, 5);

                //load 1 more entry and confirm the changes
                loadEntriesInMintDB(1);

                runIndexerAndVerifyResult(presentationStatement, indexEntryItemTask, 8, 5);

                //run indexer again when no new entries available, confirms that nothing changes but cloudwatch get notification with 0 entry
                runIndexerAndVerifyResult(presentationStatement, indexEntryItemTask, 8, 5);
            }
        }
    }

    private void runIndexerAndVerifyResult(Statement statement, IndexEntryItemTask indexerEntryItemTask, int expectedEntries, int expectedItems) throws SQLException {
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

    private void loadEntriesInMintDB(int noOfEntries) throws SQLException {
        try (Statement statement = createMintConnection().createStatement()) {
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

    private void recreateEntryAndItemTables(Statement statement) throws SQLException {
        statement.execute("drop table if exists entry");
        statement.execute("drop table if exists item");
        statement.execute("create table if not exists entry (entry_number serial primary key, sha256hex varchar, timestamp timestamp default now())");
        statement.execute("create table if not exists item (sha256hex varchar primary key, content bytea)");
    }

    private void dropReadApiTables(Statement statement) throws SQLException {
        statement.execute("drop table if exists entry");
        statement.execute("drop table if exists item");
    }

    private Connection createMintConnection() throws SQLException {
        PGSimpleDataSource dataSource = new PGSimpleDataSource();
        dataSource.setDatabaseName("test_indexer_mint");
        dataSource.setUser("postgres");
        return dataSource.getConnection();
    }

    private Connection createPresentationConnection() throws SQLException {
        PGSimpleDataSource dataSource = new PGSimpleDataSource();
        dataSource.setDatabaseName("test_indexer_presentation");
        dataSource.setUser("postgres");
        return dataSource.getConnection();
    }
}
