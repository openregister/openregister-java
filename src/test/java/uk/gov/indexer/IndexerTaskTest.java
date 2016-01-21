package uk.gov.indexer;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.hamcrest.CoreMatchers;
import org.junit.Rule;
import org.junit.Test;
import org.postgresql.ds.PGSimpleDataSource;
import org.skife.jdbi.v2.DBI;
import uk.gov.indexer.ctserver.CTServer;
import uk.gov.indexer.dao.DestinationDBUpdateDAO;
import uk.gov.indexer.dao.SourceDBQueryDAO;
import uk.gov.indexer.fetchers.CTDataSource;
import uk.gov.indexer.fetchers.DataSource;
import uk.gov.indexer.fetchers.PGDataSource;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.hamcrest.MatcherAssert.assertThat;

public class IndexerTaskTest {
    @Rule
    public WireMockRule wireMockRule = new WireMockRule(8090);

    @SuppressWarnings("ConstantConditions")
    @Test
    public void readEntriesFromMintCTServerAndWriteToReadApi() throws InterruptedException, SQLException {
        stubFor(get(urlEqualTo("/ct/v1/get-sth"))
                .willReturn(aResponse()
                                .withStatus(200)
                                .withBody("{ " +
                                        "\"tree_size\": 5, " +
                                        "\"timestamp\": 1447421303202, " +
                                        "\"sha256_root_hash\": \"47DEQpj8HBSa+\\/TImW+5JCeuQeRkm5NMpJWZG3hSuFU=\", " +
                                        "\"tree_head_signature\": \"BAMARjBEAiAQnnVGk3koQHBwvUhcLr\\/YVglyvKjfPGNmOknSY6Uk8gIgfcFDQcJUkM2Lhv4dhY6TFX96LfrOIJioQTR00bZcm7Q=\" " +
                                        "}")
                ));

        stubFor(get(urlEqualTo("/ct/v1/get-entries?start=0&end=4"))
                .willReturn(aResponse()
                                .withStatus(200)
                                .withBody("{ \"entries\": " +
                                        "[ " +
                                        "{ \"leaf_input\": \"AAAAAAFSIJm7NoAAAACMeyAiYnVzaW5lc3MiOiAiY29tcGFueTowNzIyODEzMCIsICJlbmQtZGF0ZSI6ICIiLCAiZm9vZC1wcmVtaXNlcyI6ICI3NTkzMzIiLCAiZm9vZC1wcmVtaXNlcy10eXBlcyI6IFsgXSwgIm5hbWUiOiAiQnlyb24iLCAic3RhcnQtZGF0ZSI6ICIiIH0AAA==\", \"extra_data\": \"\" }, " +
                                        "{ \"leaf_input\": \"AAAAAAFSIJm7NoAAAACMeyAiYnVzaW5lc3MiOiAiY29tcGFueTowNzIyODEzMCIsICJlbmQtZGF0ZSI6ICIiLCAiZm9vZC1wcmVtaXNlcyI6ICI3NTkzMzIiLCAiZm9vZC1wcmVtaXNlcy10eXBlcyI6IFsgXSwgIm5hbWUiOiAiQnlyb24iLCAic3RhcnQtZGF0ZSI6ICIiIH0AAA==\", \"extra_data\": \"\" } " +
                                        "] " +
                                        "}")
                ));
        stubFor(get(urlEqualTo("/ct/v1/get-entries?start=2&end=4"))
                .willReturn(aResponse()
                                .withStatus(200)
                                .withBody("{ \"entries\": " +
                                        "[ " +
                                        "{ \"leaf_input\": \"AAAAAAFSIJm7NoAAAACMeyAiYnVzaW5lc3MiOiAiY29tcGFueTowNzIyODEzMCIsICJlbmQtZGF0ZSI6ICIiLCAiZm9vZC1wcmVtaXNlcyI6ICI3NTkzMzIiLCAiZm9vZC1wcmVtaXNlcy10eXBlcyI6IFsgXSwgIm5hbWUiOiAiQnlyb24iLCAic3RhcnQtZGF0ZSI6ICIiIH0AAA==\", \"extra_data\": \"\" }, " +
                                        "{ \"leaf_input\": \"AAAAAAFSIJm7NoAAAACMeyAiYnVzaW5lc3MiOiAiY29tcGFueTowNzIyODEzMCIsICJlbmQtZGF0ZSI6ICIiLCAiZm9vZC1wcmVtaXNlcyI6ICI3NTkzMzIiLCAiZm9vZC1wcmVtaXNlcy10eXBlcyI6IFsgXSwgIm5hbWUiOiAiQnlyb24iLCAic3RhcnQtZGF0ZSI6ICIiIH0AAA==\", \"extra_data\": \"\" } " +
                                        "] " +
                                        "}")
                ));
        stubFor(get(urlEqualTo("/ct/v1/get-entries?start=4&end=4"))
                .willReturn(aResponse()
                                .withStatus(200)
                                .withBody("{ \"entries\": " +
                                        "[ " +
                                        "{ \"leaf_input\": \"AAAAAAFSIJm7NoAAAACMeyAiYnVzaW5lc3MiOiAiY29tcGFueTowNzIyODEzMCIsICJlbmQtZGF0ZSI6ICIiLCAiZm9vZC1wcmVtaXNlcyI6ICI3NTkzMzIiLCAiZm9vZC1wcmVtaXNlcy10eXBlcyI6IFsgXSwgIm5hbWUiOiAiQnlyb24iLCAic3RhcnQtZGF0ZSI6ICIiIH0AAA==\", \"extra_data\": \"\" } " +
                                        "] " +
                                        "}")
                ));


        try (Connection connection = createConnection()) {
            try (Statement statement = connection.createStatement()) {
                dropReadApiTables(statement);

                DataSource dataSource = new CTDataSource(new CTServer("http://localhost:8090"));

                DBI destDbi = new DBI("jdbc:postgresql://localhost:5432/test_indexer?user=postgres");

                DestinationDBUpdateDAO destinationDBUpdateDAO = destDbi.open().attach(DestinationDBUpdateDAO.class);

                IndexerTask indexerTask = new IndexerTask("food-premises", dataSource, destinationDBUpdateDAO);

                indexerTask.run();


                try (ResultSet resultSet = statement.executeQuery("select count(*) from ordered_entry_index")) {
                    resultSet.next();
                    int count = resultSet.getInt("count");
                    assertThat(count, CoreMatchers.equalTo(5));
                }

                try (ResultSet resultSet = statement.executeQuery("select * from sth")) {
                    resultSet.next();
                    assertThat(resultSet.getInt("tree_size"), CoreMatchers.equalTo(5));
                    assertThat(resultSet.getLong("timestamp"), CoreMatchers.equalTo(1447421303202L));
                    assertThat(resultSet.getString("sha256_root_hash"), CoreMatchers.equalTo("47DEQpj8HBSa+/TImW+5JCeuQeRkm5NMpJWZG3hSuFU="));
                    assertThat(resultSet.getString("tree_head_signature"), CoreMatchers.equalTo("BAMARjBEAiAQnnVGk3koQHBwvUhcLr/YVglyvKjfPGNmOknSY6Uk8gIgfcFDQcJUkM2Lhv4dhY6TFX96LfrOIJioQTR00bZcm7Q="));
                }
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

                loadFiveEntriesInMintDB();

                DestinationDBUpdateDAO destinationDBUpdateDAO = dbi.open().attach(DestinationDBUpdateDAO.class);


                IndexerTask indexerTask = new IndexerTask("food-premises", new PGDataSource(sourceDBQueryDAO), destinationDBUpdateDAO);

                indexerTask.run();

                try (ResultSet resultSet = statement.executeQuery("select count(*) from ordered_entry_index")) {
                    resultSet.next();
                    int count = resultSet.getInt("count");
                    assertThat(count, CoreMatchers.equalTo(5));
                }

                try (ResultSet resultSet = statement.executeQuery("select * from sth")) {
                    resultSet.next();
                    assertThat(resultSet.getInt("tree_size"), CoreMatchers.equalTo(5));
                }



                loadFiveEntriesInMintDB();
                indexerTask.run();

                try (ResultSet resultSet = statement.executeQuery("select count(*) from ordered_entry_index")) {
                    resultSet.next();
                    int count = resultSet.getInt("count");
                    assertThat(count, CoreMatchers.equalTo(10));
                }

                try (ResultSet resultSet = statement.executeQuery("select * from sth")) {
                    resultSet.next();
                    assertThat(resultSet.getInt("tree_size"), CoreMatchers.equalTo(10));
                }
            }
        }
    }

    private void loadFiveEntriesInMintDB() throws SQLException {
        try (Statement statement = createConnection().createStatement()) {
            statement.execute("insert into entries(entry) values('{\"hash\": \"hash1\", \"entry\": {\"food-premises\":\"1\",\"business\":\"company:123\"}}')");
            statement.execute("insert into entries(entry) values('{\"hash\": \"hash2\", \"entry\": {\"food-premises\":\"2\",\"business\":\"company:124\"}}')");
            statement.execute("insert into entries(entry) values('{\"hash\": \"hash3\", \"entry\": {\"food-premises\":\"3\",\"business\":\"company:125\"}}')");
            statement.execute("insert into entries(entry) values('{\"hash\": \"hash4\", \"entry\": {\"food-premises\":\"4\",\"business\":\"company:126\"}}')");
            statement.execute("insert into entries(entry) values('{\"hash\": \"hash5\", \"entry\": {\"food-premises\":\"5\",\"business\":\"company:127\"}}')");
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
