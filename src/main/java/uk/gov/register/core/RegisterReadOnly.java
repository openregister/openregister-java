package uk.gov.register.core;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface RegisterReadOnly {
    Optional<Entry> getEntry(int entryNumber);

    Optional<Item> getItemBySha256(String sha256hex);

    int getTotalEntries();

    Collection<Entry> getEntries(int start, int limit);

    Optional<Record> getRecord(String key); // TODO: is "record" the right word?

    Collection<Entry> getAllEntries();

    Collection<Item> getAllItems();

    int getTotalRecords();
    Collection<Entry> allEntriesOfRecord(String key);
    List<Record> getRecords(int limit, int offset);

    List<Record> max100RecordsFacetedByKeyValue(String key, String value);

    Optional<Instant> getLastUpdatedTime();
}
