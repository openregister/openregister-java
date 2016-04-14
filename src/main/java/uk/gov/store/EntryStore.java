package uk.gov.store;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.skife.jdbi.v2.Handle;
import org.skife.jdbi.v2.TransactionIsolationLevel;
import org.skife.jdbi.v2.sqlobject.Transaction;
import org.skife.jdbi.v2.sqlobject.mixins.GetHandle;
import uk.gov.mint.CanonicalJsonMapper;
import uk.gov.mint.Digest;
import uk.gov.mint.Item;

import java.util.Set;

public abstract class EntryStore implements GetHandle {
    private final EntryDAO entryDAO;
    private final ItemDAO itemDAO;
    private final EntriesUpdateDAO entriesUpdateDAO;
    private final CanonicalJsonMapper canonicalJsonMapper;

    public EntryStore() {
        this.canonicalJsonMapper = new CanonicalJsonMapper();

        Handle handle = getHandle();
        this.entriesUpdateDAO = handle.attach(EntriesUpdateDAO.class);
        this.entryDAO = handle.attach(EntryDAO.class);
        this.itemDAO = handle.attach(ItemDAO.class);
        entriesUpdateDAO.ensureTableExists();
        entryDAO.ensureSchema();
        itemDAO.ensureSchema();
    }

    @Transaction(TransactionIsolationLevel.SERIALIZABLE)
    public void load(Iterable<JsonNode> itemNodes) {
        Iterable<byte[]> entriesAsBytes = Iterables.transform(itemNodes, itemNode -> canonicalJsonMapper.writeToBytes(hashedEntry(itemNode)));
        entriesUpdateDAO.add(entriesAsBytes);

        Iterable<Item> items = Iterables.transform(itemNodes, Item::new);
        entryDAO.insertInBatch(Iterables.transform(items, Item::getSha256hex));
        itemDAO.insertInBatch(extractNewItems(items));
    }

    private Set<Item> extractNewItems(Iterable<Item> items) {
        Iterable<String> existingItemHex = itemDAO.existingItemHex(Lists.newArrayList(Iterables.transform(items, Item::getSha256hex)));
        return ImmutableSet.copyOf(
                Iterables.filter(
                        items,
                        item -> !Iterables.contains(existingItemHex, item.getSha256hex())
                )
        );
    }

    private ObjectNode hashedEntry(JsonNode entryJsonNode) {
        ObjectNode jsonNode = JsonNodeFactory.instance.objectNode();
        jsonNode.put("hash", Digest.shasum(entryJsonNode.toString()));
        jsonNode.set("entry", entryJsonNode);
        return jsonNode;
    }

}

