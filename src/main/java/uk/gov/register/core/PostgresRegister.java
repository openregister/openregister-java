package uk.gov.register.core;

import uk.gov.register.configuration.RegisterNameConfiguration;
import uk.gov.register.db.DestinationDBUpdateDAO;
import uk.gov.register.db.EntryDAO;
import uk.gov.register.db.EntryQueryDAO;
import uk.gov.register.db.ItemDAO;
import uk.gov.register.db.ItemQueryDAO;
import uk.gov.register.db.RecordQueryDAO;
import uk.gov.register.service.VerifiableLogService;
import uk.gov.register.views.ConsistencyProof;
import uk.gov.register.views.EntryProof;
import uk.gov.register.views.RegisterProof;

import javax.inject.Inject;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class PostgresRegister implements Register {

    private final EntryDAO entryDAO;
    private final EntryQueryDAO entryQueryDAO;
    private final ItemDAO itemDAO;
    private final ItemQueryDAO itemQueryDAO;
    private final DestinationDBUpdateDAO destinationDBUpdateDAO;
    private final RecordQueryDAO recordQueryDAO;
    private final VerifiableLogService verifiableLogService;
    private final String registerName;

    @Inject
    public PostgresRegister(EntryDAO entryDAO,
                            EntryQueryDAO entryQueryDAO,
                            ItemDAO itemDAO,
                            ItemQueryDAO itemQueryDAO,
                            DestinationDBUpdateDAO destinationDBUpdateDAO,
                            RecordQueryDAO recordQueryDAO,
                            VerifiableLogService verifiableLogService,
                            RegisterNameConfiguration registerNameConfig) {
        this.entryDAO = entryDAO;
        this.entryQueryDAO = entryQueryDAO;
        this.itemDAO = itemDAO;
        this.itemQueryDAO = itemQueryDAO;
        this.destinationDBUpdateDAO = destinationDBUpdateDAO;
        this.recordQueryDAO = recordQueryDAO;
        this.verifiableLogService = verifiableLogService;
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
    public int getTotalEntries() {
        return entryDAO.currentEntryNumber();
    }

    @Override
    public Collection<Entry> getEntries(int start, int limit) {
        return entryQueryDAO.getEntries(start, limit);
    }

    @Override
    public Optional<Record> getRecord(String key) {
        return recordQueryDAO.findByPrimaryKey(key);
    }

    @Override
    public Collection<Entry> allEntriesOfRecord(String key) {
        return recordQueryDAO.findAllEntriesOfRecordBy(registerName, key);
    }

    @Override
    public List<Record> getRecords(int limit, int offset) {
        return recordQueryDAO.getRecords(limit, offset);
    }

    @Override
    public Collection<Entry> getAllEntries() {
        return entryQueryDAO.getAllEntriesNoPagination();
    }

    @Override
    public Collection<Item> getAllItems() {
        return itemQueryDAO.getAllItemsNoPagination();
    }

    @Override
    public int getTotalRecords() {
        return recordQueryDAO.getTotalRecords();
    }

    @Override
    public Optional<Instant> getLastUpdatedTime() {
        return entryQueryDAO.getLastUpdatedTime();
    }

    @Override
    public List<Record> max100RecordsFacetedByKeyValue(String key, String value) {
        return recordQueryDAO.findMax100RecordsByKeyValue(key, value);
    }

    @Override
    public RegisterProof getRegisterProof() throws NoSuchAlgorithmException {
        return verifiableLogService.getRegisterProof();
    }

    @Override
    public EntryProof getEntryProof(int entryNumber, int totalEntries) {
        return verifiableLogService.getEntryProof(entryNumber, totalEntries);
    }

    @Override
    public ConsistencyProof getConsistencyProof(int totalEntries1, int totalEntries2) {
        return verifiableLogService.getConsistencyProof(totalEntries1, totalEntries2);
    }
}
