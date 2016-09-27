package uk.gov.register.core;

import com.google.common.collect.Iterables;
import org.skife.jdbi.v2.TransactionIsolationLevel;
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
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

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
    public void mintItems(Iterable<Item> items) {
        entryQueryDAO.inTransaction(TransactionIsolationLevel.SERIALIZABLE, (entryQueryDAO1, status)-> {
            itemDAO.insertInBatch(items);
            AtomicInteger currentEntryNumber = new AtomicInteger(getTotalEntries());
            List<Record> records = StreamSupport.stream(items.spliterator(), false)
                    .map(item -> new Record(new Entry(currentEntryNumber.incrementAndGet(), item.getSha256hex(), Instant.now()), item))
                    .collect(Collectors.toList());
            entryDAO.insertInBatch(Iterables.transform(records, r -> r.entry));
            destinationDBUpdateDAO.upsertInCurrentKeysTable(registerName, records);
            entryDAO.setEntryNumber(currentEntryNumber.get());
            return 0;
        });
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
