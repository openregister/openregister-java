package uk.gov.store;

import java.util.Arrays;

public class DataStoreImpl implements DataStore{

    @Override
    public void add(byte[] message) {
        System.out.println("message = " + Arrays.toString(message));
    }
}
