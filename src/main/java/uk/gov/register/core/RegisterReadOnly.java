package uk.gov.register.core;

import java.util.Optional;

public interface RegisterReadOnly {
    Optional<Entry> getEntry(int entryNumber);

    Optional<Item> getItemBySha256(String sha256hex);

    int currentEntryNumber();
}
