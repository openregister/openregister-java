package uk.gov;

class IndexerTask implements Runnable {
    private final String register;
    private final Indexer indexer;

    public IndexerTask(String register, SourcePostgresDB sourceDB, DestinationPostgresDB destinationDB) {
        this.register = register;
        this.indexer = new Indexer(sourceDB, destinationDB);
    }

    @Override
    public void run() {
        //noinspection InfiniteLoopStatement
        while (true) {
            try {
                ConsoleLogger.log("Starting index update for register : " + register);
                indexer.update();
                ConsoleLogger.log("Index update completed for register: " + register);
                ConsoleLogger.log("Waiting 10 seconds for register: " + register);
                Thread.sleep(10000);
            } catch (Exception e) {
                throw new RuntimeException("todo: define exception ", e);
            }
        }
    }
}
