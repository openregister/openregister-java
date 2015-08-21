package uk.gov;

import java.sql.ResultSet;
import java.sql.SQLException;

public class Indexer {
    private final SourcePostgresDB sourceDB;
    private final DestinationPostgresDB destinationDB;

    public Indexer(SourcePostgresDB sourceDB, DestinationPostgresDB destinationDB) {
        this.sourceDB = sourceDB;
        this.destinationDB = destinationDB;
    }

    public void update() throws SQLException {
        int currentWaterMark = destinationDB.currentWaterMark();
        ResultSet difference = sourceDB.read(currentWaterMark);
        destinationDB.write(difference);
    }


}
