package uk.gov;

import java.sql.ResultSet;
import java.sql.SQLException;

public class IndexerTask implements Runnable {
    private final String register;
    private final SourcePostgresDB sourceDB;
    private final DestinationPostgresDB destinationDB;

    public IndexerTask(String register, SourcePostgresDB sourceDB, DestinationPostgresDB destinationDB) {
        this.register = register;
        this.sourceDB = sourceDB;
        this.destinationDB = destinationDB;
    }

    @Override
    public void run() {
        try {
            update();
            ConsoleLogger.log("Index update completed for register: " + register);
        } catch (Throwable e) {
            e.printStackTrace();
            throw e;
        }
    }

    protected void update() {
        try {
            int currentWaterMark = destinationDB.currentWaterMark();
            ResultSet difference = sourceDB.read(currentWaterMark);
            destinationDB.write(difference);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
