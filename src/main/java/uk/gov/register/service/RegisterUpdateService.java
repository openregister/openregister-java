package uk.gov.register.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.register.core.Register;
import uk.gov.register.serialization.RegisterCommand;

import javax.inject.Inject;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class RegisterUpdateService {

    private final static Logger LOG = LoggerFactory.getLogger(RegisterUpdateService.class);

    private final RegisterService registerService;

    @Inject
    public RegisterUpdateService(RegisterService registerService) {
        this.registerService = registerService;
    }

    public void processRegisterComponents(List<RegisterCommand> commands) {
        registerService.asAtomicRegisterOperation(register -> mintRegisterComponents(commands, register));
    }

    private void mintRegisterComponents(List<RegisterCommand> commands, Register register) {
        final int startEntryNum = register.getTotalEntries();
        AtomicInteger entryNum = new AtomicInteger(startEntryNum);
        commands.forEach(c -> c.execute(register, entryNum));
    }
}
