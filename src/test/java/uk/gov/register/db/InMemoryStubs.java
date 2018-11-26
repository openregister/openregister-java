package uk.gov.register.db;

import uk.gov.register.core.EntryLog;
import uk.gov.register.core.InMemoryEntryLog;
import uk.gov.verifiablelog.store.memoization.DoNothing;

public abstract class InMemoryStubs {
    public static EntryLog inMemoryEntryLog(InMemoryEntryDAO entryQueryDAO, EntryDAO entryDAO) {
        return new InMemoryEntryLog(new DoNothing(), entryQueryDAO, entryDAO);
    }
}
