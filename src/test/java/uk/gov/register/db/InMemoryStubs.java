package uk.gov.register.db;

import uk.gov.register.core.EntryLog;
import uk.gov.register.core.InMemoryEntryLog;
import uk.gov.register.core.InMemoryBlobStore;
import uk.gov.register.core.BlobStore;
import uk.gov.register.service.BlobValidator;
import uk.gov.verifiablelog.store.memoization.DoNothing;

import java.util.HashMap;

public abstract class InMemoryStubs {
    public static EntryLog inMemoryEntryLog(InMemoryEntryDAO entryQueryDAO, EntryDAO entryDAO) {
        return new InMemoryEntryLog(new DoNothing(), entryQueryDAO, entryDAO);
    }

    public static BlobStore inMemoryItemStore(BlobValidator blobValidator, InMemoryEntryDAO entryDAO) {
        InMemoryBlobDAO itemDAO = new InMemoryBlobDAO(new HashMap<>(), entryDAO);
        return new InMemoryBlobStore(itemDAO, itemDAO);
    }
}
