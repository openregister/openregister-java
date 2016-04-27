package uk.gov.register.presentation.functional;

import java.time.Instant;

public class TestEntry {
    public final int entryNumber;
    public final String itemJson;
    public final Instant entryTimestamp;

    private TestEntry(int entryNumber, String itemJson, Instant entryTimestamp) {
        this.entryNumber = entryNumber;
        this.itemJson = itemJson;
        this.entryTimestamp = entryTimestamp;
    }

    public static TestEntry anEntry(int entryNumber, String itemJson){
        return new TestEntry(entryNumber, itemJson, Instant.now());

    }
    public static TestEntry anEntry(int entryNumber, String itemJson, Instant entryTimestamp){
        return new TestEntry(entryNumber, itemJson, entryTimestamp);
    }
}
