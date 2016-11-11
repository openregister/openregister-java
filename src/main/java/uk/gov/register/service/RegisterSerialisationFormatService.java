package uk.gov.register.service;

import org.apache.jena.ext.com.google.common.collect.Iterators;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.register.core.Register;
import uk.gov.register.core.RegisterReadOnly;
import uk.gov.register.serialization.AddItemCommand;
import uk.gov.register.serialization.AppendEntryCommand;
import uk.gov.register.serialization.RegisterCommand;
import uk.gov.register.serialization.RegisterSerialisationFormat;

import javax.inject.Inject;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;

public class RegisterSerialisationFormatService {

    private final static Logger LOG = LoggerFactory.getLogger(RegisterSerialisationFormatService.class);

    private final RegisterService registerService;
    private RegisterReadOnly register;

    @Inject
    public RegisterSerialisationFormatService(RegisterService registerService, RegisterReadOnly register) {
        this.registerService = registerService;
        this.register = register;
    }

    public void processRegisterComponents(RegisterSerialisationFormat rsf) {
        registerService.asAtomicRegisterOperation(register -> mintRegisterComponents(rsf.getCommands(), register));
    }

    public RegisterSerialisationFormat createRegisterSerialisationFormat(){
        Iterator<RegisterCommand> itemCommandsIterator = Iterators.transform(register.getItemIterator(), AddItemCommand::new);
        Iterator<RegisterCommand> entryCommandIterator = Iterators.transform(register.getEntryIterator(), AppendEntryCommand::new);

        return new RegisterSerialisationFormat(Iterators.concat(itemCommandsIterator, entryCommandIterator));
    }

    public RegisterSerialisationFormat createRegisterSerialisationFormat(int startEntryNo, int endEntryNo){
        Iterator<RegisterCommand> itemCommandsIterator = Iterators.transform(register.getItemIterator(startEntryNo, endEntryNo), AddItemCommand::new);
        Iterator<RegisterCommand> entryCommandIterator = Iterators.transform(register.getEntryIterator(startEntryNo, endEntryNo), AppendEntryCommand::new);

        return new RegisterSerialisationFormat(Iterators.concat(itemCommandsIterator, entryCommandIterator));
    }

    private void mintRegisterComponents(Iterator<RegisterCommand> commands, Register register) {
        final int startEntryNum = register.getTotalEntries();
        AtomicInteger entryNum = new AtomicInteger(startEntryNum);
        commands.forEachRemaining(c -> c.execute(register, entryNum));
    }

}
