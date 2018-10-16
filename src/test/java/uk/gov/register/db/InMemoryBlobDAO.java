package uk.gov.register.db;

import org.apache.commons.lang3.NotImplementedException;
import org.skife.jdbi.v2.sqlobject.Bind;
import uk.gov.register.core.Blob;
import uk.gov.register.core.Entry;
import uk.gov.register.store.postgres.BindBlob;
import uk.gov.register.util.HashValue;

import java.util.*;
import java.util.stream.Collectors;

import static uk.gov.register.core.HashingAlgorithm.SHA256;

public class InMemoryBlobDAO implements BlobDAO, BlobQueryDAO {
    private final Map<HashValue, Blob> items;
    private EntryQueryDAO entryQueryDao;

    public InMemoryBlobDAO(Map<HashValue, Blob> items, EntryQueryDAO entryQueryDao) {
        this.items = items;
        this.entryQueryDao = entryQueryDao;
    }

    @Override
    public void insertInBatch(@BindBlob Iterable<Blob> items, String schema) {
        for (Blob blob : items) {
            this.items.put(blob.getSha256hex(), blob);
        }
    }

    @Override
    public Optional<Blob> getItemBySHA256(@Bind("sha256hex") String sha256Hash, String schema) {
        return Optional.ofNullable(items.get(new HashValue(SHA256, sha256Hash)));
    }

    @Override
    public Collection<Blob> getAllItemsNoPagination(String schema) {
        return items.values();
    }

    @Override
    public Iterator<Blob> getIterator(String schema) {
        return getItemIteratorFromEntryIterator(entryQueryDao.getIterator(schema));
    }

    @Override
    public Iterator<Blob> getIterator(@Bind("startEntryNo") int startEntryNo, @Bind("endEntryNo") int endEntryNo, String schema) {
        return getItemIteratorFromEntryIterator(entryQueryDao.getIterator(startEntryNo, endEntryNo, schema));
    }

    @Override
    public Iterator<Blob> getSystemItemIterator(String schema) {
        throw new NotImplementedException("Not yet implemented");
    }

    private Iterator<Blob> getItemIteratorFromEntryIterator(Iterator<Entry> entryIterator) {
        List<Blob> itemsResult = new ArrayList<>();
        entryIterator.forEachRemaining(entry -> {
            List<HashValue> hashValues = items.keySet().stream().filter(hashValue -> entry.getBlobHashes().contains(hashValue)).collect(Collectors.toList());
            hashValues.forEach(hashValue -> itemsResult.add(items.remove(hashValue)));
        });
        return itemsResult.iterator();
    }
}
