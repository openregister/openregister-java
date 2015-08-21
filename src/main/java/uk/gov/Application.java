package uk.gov;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Properties;

public class Application {

    public static void main(String[] args) throws IOException, SQLException {
        Properties  properties = new Properties();
        properties.load(Application.class.getResourceAsStream("/application.properties"));

        String registerName = properties.getProperty("register.name");
        SourcePostgresDB sourceDB = new SourcePostgresDB(registerName, properties.getProperty("source.postgres.db.connectionString"));
        DestinationPostgresDB destinationDB = new DestinationPostgresDB(registerName, properties.getProperty("destination.postgres.db.connectionString"));

        Indexer indexer = new Indexer(sourceDB, destinationDB);
        indexer.update();
    }
}
