package uk.gov.register.service;

import org.apache.jena.ext.com.google.common.collect.Iterators;
import uk.gov.register.core.Register;
import uk.gov.register.core.RegisterReadOnly;
import uk.gov.register.exceptions.RootHashAssertionException;
import uk.gov.register.serialization.*;
import uk.gov.register.views.RegisterProof;

import javax.inject.Inject;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;

public class RegisterSerialisationFormatService {

    private final String EMPTY_ROOT_HASH = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855";
    private final RegisterProof emptyRegisterProof;

    private final RegisterService registerService;
    private RegisterReadOnly register;

    @Inject
    public RegisterSerialisationFormatService(RegisterService registerService, RegisterReadOnly register) {
        this.registerService = registerService;
        this.register = register;
        this.emptyRegisterProof = new RegisterProof(EMPTY_ROOT_HASH);
    }

    public void processRegisterComponents(RegisterSerialisationFormat rsf) {
        registerService.asAtomicRegisterOperation(register -> mintRegisterComponents(rsf.getCommands(), register));
    }

    public RegisterSerialisationFormat createRegisterSerialisationFormat() {
        Iterator<RegisterCommand> itemCommandsIterator = Iterators.transform(register.getItemIterator(), AddItemCommand::new);
        Iterator<RegisterCommand> entryCommandIterator = Iterators.transform(register.getEntryIterator(), AppendEntryCommand::new);

        try {
            return new RegisterSerialisationFormat(Iterators.concat(
                    Iterators.forArray(new AssertRootHashCommand(emptyRegisterProof)),
                    itemCommandsIterator,
                    entryCommandIterator,
                    Iterators.forArray(new AssertRootHashCommand(register.getRegisterProof()))
            ));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

    }

    public RegisterSerialisationFormat createRegisterSerialisationFormat(int startEntryNo, int endEntryNo) {
        Iterator<RegisterCommand> itemCommandsIterator = Iterators.transform(register.getItemIterator(startEntryNo, endEntryNo), AddItemCommand::new);
        Iterator<RegisterCommand> entryCommandIterator = Iterators.transform(register.getEntryIterator(startEntryNo, endEntryNo), AppendEntryCommand::new);

        RegisterProof previousRegisterProof = startEntryNo == 1 ? emptyRegisterProof : register.getRegisterProof(startEntryNo - 1, startEntryNo - 1);

        return new RegisterSerialisationFormat(Iterators.concat(
                Iterators.forArray(new AssertRootHashCommand(previousRegisterProof)),
                itemCommandsIterator,
                entryCommandIterator,
                Iterators.forArray(new AssertRootHashCommand(register.getRegisterProof(startEntryNo, endEntryNo)))));
    }

    private void mintRegisterComponents(Iterator<RegisterCommand> commands, Register register) {
        final int startEntryNum = register.getTotalEntries() + 1;
        AtomicInteger entryNum = new AtomicInteger(startEntryNum);
        commands.forEachRemaining(c -> {
            try {
                c.execute(register, entryNum);
            }catch(RootHashAssertionException e){
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        });
    }
}