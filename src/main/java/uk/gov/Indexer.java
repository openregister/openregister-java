package uk.gov;

import java.sql.SQLException;

public class Indexer {
    private final SourcePostgresDB sourceDB;
    private final DestinationPostgresDB destinationDB;

    public Indexer(SourcePostgresDB sourceDB, DestinationPostgresDB destinationDB) {
        this.sourceDB = sourceDB;
        this.destinationDB = destinationDB;
    }

    public void update() throws SQLException {
        destinationDB.write(sourceDB.read());
    }


}
