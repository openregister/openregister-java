package uk.gov.register.db;

import uk.gov.register.core.EntryLog;
import uk.gov.register.core.InMemoryEntryLog;
import uk.gov.register.core.ItemStore;
import uk.gov.register.service.ItemValidator;
import uk.gov.verifiablelog.store.memoization.DoNothing;

import java.util.HashMap;

public abstract class InMemoryStubs {
    public static EntryLog inMemoryEntryLog(InMemoryEntryDAO entryQueryDAO) {
        return new InMemoryEntryLog(new DoNothing(), entryQueryDAO, entryQueryDAO);
    }

    public static ItemStore inMemoryItemStore(ItemValidator itemValidator, InMemoryEntryDAO entryQueryDao) {
        InMemoryItemDAO itemDao = new InMemoryItemDAO(new HashMap<>(), entryQueryDao);
        return new OnDemandItemStore(itemValidator, itemDao, itemDao);
    }
}
