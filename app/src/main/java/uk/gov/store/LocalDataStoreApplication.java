package uk.gov.store;

import uk.gov.integration.DataStoreApplication;

public class LocalDataStoreApplication implements DataStoreApplication {
    private DataStore dataStore;

    public LocalDataStoreApplication(DataStore dataStore) {
        this.dataStore = dataStore;
    }

    @Override
    public void add(byte[] message) {
        dataStore.add(message);
    }
}
