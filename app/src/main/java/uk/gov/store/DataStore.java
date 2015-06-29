package uk.gov.store;

public interface DataStore {
    void add(byte[] message);

    void close() throws Exception;
}
