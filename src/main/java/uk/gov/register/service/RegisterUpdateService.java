package uk.gov.register.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.register.core.Entry;
import uk.gov.register.core.Register;
import uk.gov.register.exceptions.SerializedRegisterParseException;
import uk.gov.register.serialization.RegisterComponents;

import javax.inject.Inject;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class RegisterUpdateService {

    private final static Logger LOG = LoggerFactory.getLogger(RegisterUpdateService.class);

    private final RegisterService registerService;

    @Inject
    public RegisterUpdateService(RegisterService registerService) {
        this.registerService = registerService;
    }

    public void processRegisterComponents(RegisterComponents registerComponents) {
            mintRegisterComponentsInTransaction(registerComponents);
    }

    private void processPotentialChildlessEntries(Set<Entry> potentialChildlessEntries, RegisterComponents registerComponents) {
        registerService.asAtomicRegisterOperation(register -> {
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
        registerService.asAtomicRegisterOperation(register -> mintRegisterComponents(registerComponents, register));
    }

    private void mintRegisterComponents(RegisterComponents registerComponents, Register register) {
        final int startEntryNum = register.getTotalEntries();
        AtomicInteger entryNum = new AtomicInteger(startEntryNum);
        registerComponents.commands.forEach( c -> c.execute(register));
    }
}
