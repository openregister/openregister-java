package uk.gov.register.presentation.functional;

import java.time.Instant;

public class TestEntry {
    public final int entryNumber;
    public final String itemJson;
    public final Instant entryTimestamp;

    public TestEntry(int entryNumber, String itemJson, Instant entryTimestamp) {
        this.entryNumber = entryNumber;
        this.itemJson = itemJson;
        this.entryTimestamp = entryTimestamp;
    }
}
