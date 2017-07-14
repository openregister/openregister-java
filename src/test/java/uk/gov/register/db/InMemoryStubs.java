package uk.gov.register.db;

import uk.gov.register.core.EntryLog;
import uk.gov.register.core.InMemoryEntryLog;
import uk.gov.register.core.InMemoryItemStore;
import uk.gov.register.core.ItemStore;
import uk.gov.register.service.ItemValidator;
import uk.gov.verifiablelog.store.memoization.DoNothing;

import java.util.HashMap;

public abstract class InMemoryStubs {
    public static EntryLog inMemoryEntryLog(InMemoryEntryDAO entryQueryDAO, EntryDAO entryDAO) {
        return new InMemoryEntryLog(new DoNothing(), entryQueryDAO, entryDAO);
    }

    public static ItemStore inMemoryItemStore(ItemValidator itemValidator, InMemoryEntryDAO entryDAO) {
        InMemoryItemDAO itemDAO = new InMemoryItemDAO(new HashMap<>(), entryDAO);
        return new InMemoryItemStore(itemDAO, itemDAO);
    }
}
