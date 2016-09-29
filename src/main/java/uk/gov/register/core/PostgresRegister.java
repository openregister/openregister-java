package uk.gov.register.core;

import com.google.common.collect.Iterables;
import org.skife.jdbi.v2.TransactionIsolationLevel;
import uk.gov.register.configuration.RegisterNameConfiguration;
import uk.gov.register.db.RegisterDAO;
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
    private final RegisterDAO registerDAO;

    private final VerifiableLogService verifiableLogService;
    private final String registerName;

    @Inject
    public PostgresRegister(VerifiableLogService verifiableLogService,
                            RegisterNameConfiguration registerNameConfig,
                            RegisterDAO registerDAO) {
        this.verifiableLogService = verifiableLogService;
        this.registerDAO = registerDAO;
        registerName = registerNameConfig.getRegister();
    }

    @Override
    public void mintItems(Iterable<Item> items) {
        registerDAO.inTransaction(TransactionIsolationLevel.SERIALIZABLE, (txnRegisterDAO, status)-> {
            txnRegisterDAO.batchInsertItems(items);
            AtomicInteger currentEntryNumber = new AtomicInteger(txnRegisterDAO.getTotalEntries());
            List<Record> records = StreamSupport.stream(items.spliterator(), false)
                    .map(item -> new Record(new Entry(currentEntryNumber.incrementAndGet(), item.getSha256hex(), Instant.now()), item))
                    .collect(Collectors.toList());
            txnRegisterDAO.batchInsertEntries(Iterables.transform(records, r -> r.entry));
            txnRegisterDAO.upsertInCurrentKeysTable(registerName, records);
            txnRegisterDAO.setEntryNumber(currentEntryNumber.get());
            return 0;
        });
    }

    @Override
    public Optional<Entry> getEntry(int entryNumber) {
        return registerDAO.getEntry(entryNumber);
    }

    @Override
    public Optional<Item> getItemBySha256(String sha256hex) {
        return registerDAO.getItemBySha256(sha256hex);
    }

    @Override
    public int getTotalEntries() {
        return registerDAO.getTotalEntries();
    }

    @Override
    public Collection<Entry> getEntries(int start, int limit) {
        return registerDAO.getEntries(start, limit);
    }

    @Override
    public Optional<Record> getRecord(String key) {
        return registerDAO.getRecord(key);
    }

    @Override
    public Collection<Entry> allEntriesOfRecord(String key) {
        return registerDAO.findAllEntriesOfRecordBy(registerName, key);
    }

    @Override
    public List<Record> getRecords(int limit, int offset) {
        return registerDAO.getRecords(limit, offset);
    }

    @Override
    public Collection<Entry> getAllEntries() {
        return registerDAO.getAllEntries();
    }

    @Override
    public Collection<Item> getAllItems() {
        return registerDAO.getAllItems();
    }

    @Override
    public int getTotalRecords() {
        return registerDAO.getTotalRecords();
    }

    @Override
    public Optional<Instant> getLastUpdatedTime() {
        return registerDAO.getLastUpdatedTime();
    }

    @Override
    public List<Record> max100RecordsFacetedByKeyValue(String key, String value) {
        return registerDAO.findMax100RecordsByKeyValue(key, value);
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
