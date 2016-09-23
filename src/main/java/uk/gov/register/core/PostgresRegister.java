package uk.gov.register.core;

import uk.gov.register.configuration.RegisterNameConfiguration;
import uk.gov.register.db.DestinationDBUpdateDAO;
import uk.gov.register.db.EntryDAO;
import uk.gov.register.db.EntryQueryDAO;
import uk.gov.register.db.ItemDAO;
import uk.gov.register.db.ItemQueryDAO;

import javax.inject.Inject;
import java.util.Collections;
import java.util.Optional;

public class PostgresRegister implements Register {

    private final EntryDAO entryDAO;
    private final EntryQueryDAO entryQueryDAO;
    private final ItemDAO itemDAO;
    private final ItemQueryDAO itemQueryDAO;
    private final DestinationDBUpdateDAO destinationDBUpdateDAO;
    private final String registerName;

    @Inject
    public PostgresRegister(EntryDAO entryDAO, EntryQueryDAO entryQueryDAO, ItemDAO itemDAO, ItemQueryDAO itemQueryDAO, DestinationDBUpdateDAO destinationDBUpdateDAO, RegisterNameConfiguration registerNameConfig) {
        this.entryDAO = entryDAO;
        this.entryQueryDAO = entryQueryDAO;
        this.itemDAO = itemDAO;
        this.itemQueryDAO = itemQueryDAO;
        this.destinationDBUpdateDAO = destinationDBUpdateDAO;
        registerName = registerNameConfig.getRegister();
    }

    @Override
    public void addItem(Item item) {
        itemDAO.insertInBatch(Collections.singletonList(item));
    }

    @Override
    public void addEntry(Entry entry) {
        // TODO: do we need to check if referred item already exists?
        entryDAO.insertInBatch(Collections.singletonList(entry));
        Record fatEntry = new Record(entry, getItemBySha256(entry.getSha256hex()).get());
        destinationDBUpdateDAO.upsertInCurrentKeysTable(registerName, Collections.singletonList(fatEntry));
        entryDAO.setEntryNumber(entryDAO.currentEntryNumber() + 1);
    }

    @Override
    public void addItemAndEntry(Item item, Entry entry) {
        entryDAO.insertInBatch(Collections.singletonList(entry));
        itemDAO.insertInBatch(Collections.singletonList(item));
        // should probably check entry and item match one another
        Record fatEntry = new Record(entry, item);
        destinationDBUpdateDAO.upsertInCurrentKeysTable(registerName, Collections.singletonList(fatEntry));
    }

    @Override
    public Optional<Entry> getEntry(int entryNumber) {
        return entryQueryDAO.findByEntryNumber(entryNumber);
    }

    @Override
    public Optional<Item> getItemBySha256(String sha256hex) {
        return itemQueryDAO.getItemBySHA256(sha256hex);
    }

    @Override
    public int currentEntryNumber() {
        return entryDAO.currentEntryNumber();
    }
}
