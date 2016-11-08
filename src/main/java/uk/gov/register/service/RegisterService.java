package uk.gov.register.service;

import org.skife.jdbi.v2.DBI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.register.configuration.RegisterNameConfiguration;
import uk.gov.register.core.Entry;
import uk.gov.register.core.Item;
import uk.gov.register.core.PostgresRegister;
import uk.gov.register.core.Register;
import uk.gov.register.exceptions.SerializedRegisterParseException;
import uk.gov.register.serialization.RegisterComponents;
import uk.gov.register.store.postgres.PostgresDriverNonTransactional;
import uk.gov.register.store.postgres.PostgresDriverTransactional;
import uk.gov.register.util.OrphanFinder;
import uk.gov.verifiablelog.store.memoization.MemoizationStore;

import javax.inject.Inject;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class RegisterService {
    private final static Logger LOG = LoggerFactory.getLogger(RegisterService.class);

    private final RegisterNameConfiguration registerNameConfig;
    private final DBI dbi;
    private final MemoizationStore memoizationStore;
    private final String registerPrimaryKey;
    private final ItemValidator itemValidator;
    private final OrphanFinder orphanFinder;

    @Inject
    public RegisterService(RegisterNameConfiguration registerNameConfig, DBI dbi, MemoizationStore memoizationStore, ItemValidator itemValidator, OrphanFinder orphanFinder) {
        this.registerNameConfig = registerNameConfig;
        this.dbi = dbi;
        this.memoizationStore = memoizationStore;
        this.registerPrimaryKey = registerNameConfig.getRegister();
        this.itemValidator = itemValidator;
        this.orphanFinder = orphanFinder;
    }

    public void asAtomicRegisterOperation(Consumer<Register> callback) {
        //PostgresDriverTransactional.useTransaction(dbi, memoizationStore, postgresDriver -> {
        PostgresDriverTransactional.useTransaction(dbi, memoizationStore, postgresDriver -> {
            Register register = new PostgresRegister(registerNameConfig, postgresDriver);
            callback.accept(register);
        });
    }

    public void processRegisterComponents(RegisterComponents registerComponents) {
        Set<Item> orphanItems = orphanFinder.findOrphanItems(registerComponents.items, registerComponents.entries);
        LOG.debug("Checked orphan items");
        if (orphanItems.isEmpty()) {
            Set<Entry> potentialChildlessEntries = orphanFinder.findChildlessEntries(registerComponents.items, registerComponents.entries);
            LOG.debug("Checked for potential childless entries");
            if (potentialChildlessEntries.isEmpty()) {
                mintRegisterComponentsInTransaction(registerComponents);
            } else {
                processPotentialChildlessEntries(potentialChildlessEntries, registerComponents);
            }
        } else {
            String orphanItemsContent = orphanItems.stream().map(Item::toString).collect(Collectors.joining(" "));
            throw new SerializedRegisterParseException("Error: items with no corresponding entries in input: " + orphanItemsContent);
        }
    }

    private void processPotentialChildlessEntries(Set<Entry> potentialChildlessEntries, RegisterComponents registerComponents) {
        assert (!potentialChildlessEntries.isEmpty());
        asAtomicRegisterOperation(register -> {
            if (allEntriesMatch(potentialChildlessEntries, register)) {
                mintRegisterComponents(registerComponents, register);
            } else {
                String childlessEntriesHashes = potentialChildlessEntries.stream().map(Entry::getSha256hex).collect(Collectors.joining(" "));
                throw new SerializedRegisterParseException("Error: entry with no corresponding entry in input or existing register in: " + childlessEntriesHashes);
            }
        });
    }

    private boolean allEntriesMatch(Set<Entry> potentialChildlessEntries, Register register) {
        return potentialChildlessEntries.stream().allMatch(e -> register.getItemBySha256(e.getSha256hex()).isPresent());
    }

    private void mintRegisterComponentsInTransaction(RegisterComponents registerComponents) {
        asAtomicRegisterOperation(register -> mintRegisterComponents(registerComponents, register));
    }

    private void mintRegisterComponentsNoTransaction(RegisterComponents registerComponents) {
        PostgresDriverNonTransactional postgresDriverNonTransactional = new PostgresDriverNonTransactional(dbi, memoizationStore);
        mintRegisterComponents2(registerComponents, postgresDriverNonTransactional);
    }

    private void mintRegisterComponents(RegisterComponents registerComponents, Register register) {
        int startEntryNum = register.getTotalEntries();
        registerComponents.items.forEach(i -> {
            itemValidator.validateItem(registerPrimaryKey, i.getContent());
            register.putItem(i);
        });
        registerComponents.entries.forEach(e -> {
            register.appendEntry(new Entry(e.getEntryNumber() + startEntryNum, e.getSha256hex(), e.getTimestamp()));
        });
    }

    private void mintRegisterComponents2(RegisterComponents registerComponents, PostgresDriverNonTransactional driver) {
        int startEntryNum = driver.getTotalEntries();
        registerComponents.items.forEach(i -> {
            itemValidator.validateItem(registerPrimaryKey, i.getContent());
            driver.insertItem(i);
        });
        registerComponents.entries.forEach(e -> {
            driver.insertEntry(new Entry(e.getEntryNumber() + startEntryNum, e.getSha256hex(), e.getTimestamp()));
        });
    }

}
