package uk.gov.store;

import uk.gov.integration.DataStoreApplication;

public class LocalDataStoreApplication implements DataStoreApplication {
    private DataStore dataStore;
    private LogStream logStream;

    public LocalDataStoreApplication(DataStore dataStore, LogStream logStream) {
        this.dataStore = dataStore;
        this.logStream = logStream;
    }

    @Override
    public void add(byte[] message) {
        // TODO: this could be bad if postgres succeeds and kafka fails. we need to think about this at some point.
        dataStore.add(message);
//        logStream.send(message);
    }
}
