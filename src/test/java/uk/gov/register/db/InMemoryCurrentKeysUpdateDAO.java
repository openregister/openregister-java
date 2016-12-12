package uk.gov.register.db;

import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.BindBean;

import java.util.Map;

public class InMemoryCurrentKeysUpdateDAO implements CurrentKeysUpdateDAO {
    private final Map<String, Integer> currentKeys;

    private int totalRecords = 0;

    public InMemoryCurrentKeysUpdateDAO(Map<String, Integer> currentKeys) {
        this.currentKeys = currentKeys;
    }

    @Override
    public int[] removeRecordWithKeys(@Bind("key") Iterable<String> allKeys) {
        int total = 0;
        for (String key : allKeys) {
            if (currentKeys.containsKey(key)) {
                total++;
                currentKeys.remove(key);
            }
        }
        int[] ints = {total};
        return ints;
    }

    @Override
    public void writeCurrentKeys(@BindBean Iterable<CurrentKey> values) {
        for (CurrentKey value : values) {
            currentKeys.put(value.getKey(), value.getEntry_number());
        }
    }

    @Override
    public void updateTotalRecords(@Bind("noOfNewRecords") int noOfNewRecords) {
        this.totalRecords += noOfNewRecords;
    }

    public int getTotalRecords() {
        return totalRecords;
    }
}
