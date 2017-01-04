package uk.gov.register.service;

import org.apache.jena.ext.com.google.common.collect.Iterators;
import uk.gov.register.core.HashingAlgorithm;
import uk.gov.register.core.Register;
import uk.gov.register.core.RegisterContext;
import uk.gov.register.exceptions.RootHashAssertionException;
import uk.gov.register.serialization.*;
import uk.gov.register.util.HashValue;
import uk.gov.register.views.RegisterProof;

import javax.inject.Inject;
import java.io.OutputStream;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;
import java.util.function.Function;

public class RegisterSerialisationFormatService {

    private final String EMPTY_ROOT_HASH = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855";
    private final RegisterProof emptyRegisterProof;

    private final RegisterContext registerContext;

    @Inject
    public RegisterSerialisationFormatService(RegisterContext registerContext) {
        this.registerContext = registerContext;
        this.emptyRegisterProof = new RegisterProof(new HashValue(HashingAlgorithm.SHA256, EMPTY_ROOT_HASH));
    }

    public void processRegisterComponents(RegisterSerialisationFormat rsf) {
        registerContext.transactionalRegisterOperation(register -> mintRegisterComponents(rsf.getCommands(), register));
    }

    public void writeTo(OutputStream output, CommandParser commandParser) {
        writeTo(output, commandParser, this::createRegisterSerialisationFormat);
    }

    public void writeTo(OutputStream output, CommandParser commandParser, int totalEntries1, int totalEntries2) {
        writeTo(output, commandParser, register -> createRegisterSerialisationFormat(register, totalEntries1, totalEntries2));
    }

    public RegisterSerialisationFormat createRegisterSerialisationFormat(Register register) {
        Iterator<RegisterCommand> itemCommandsIterator = Iterators.transform(register.getItemIterator(), AddItemCommand::new);
        Iterator<RegisterCommand> entryCommandIterator = Iterators.transform(register.getEntryIterator(), AppendEntryCommand::new);

        try {
            return new RegisterSerialisationFormat(Iterators.concat(
                    Iterators.singletonIterator(new AssertRootHashCommand(emptyRegisterProof)),
                    itemCommandsIterator,
                    entryCommandIterator,
                    Iterators.singletonIterator(new AssertRootHashCommand(register.getRegisterProof()))
            ));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public RegisterSerialisationFormat createRegisterSerialisationFormat(Register register, int totalEntries1, int totalEntries2) {
        Iterator<RegisterCommand> iterators;

        if (totalEntries1 == totalEntries2) {
            iterators = Iterators.singletonIterator(new AssertRootHashCommand(register.getRegisterProof(totalEntries1)));
        } else {
            RegisterProof previousRegisterProof = totalEntries1 == 0 ? emptyRegisterProof : register.getRegisterProof(totalEntries1);
            RegisterProof nextRegisterProof = register.getRegisterProof(totalEntries2);

            iterators = Iterators.concat(
                    Iterators.singletonIterator(new AssertRootHashCommand(previousRegisterProof)),
                    Iterators.transform(register.getItemIterator(totalEntries1, totalEntries2), AddItemCommand::new),
                    Iterators.transform(register.getEntryIterator(totalEntries1, totalEntries2), AppendEntryCommand::new),
                    Iterators.singletonIterator(new AssertRootHashCommand(nextRegisterProof)));
        }

        return new RegisterSerialisationFormat(iterators);
    }

    private void mintRegisterComponents(Iterator<RegisterCommand> commands, Register register) {
        commands.forEachRemaining(c -> {
            try {
                c.execute(register);
            } catch (RootHashAssertionException e) {
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    private void writeTo(OutputStream output, CommandParser commandParser, Function<Register, RegisterSerialisationFormat> rsfCreator) {
        registerContext.transactionalRegisterOperation(register -> {
            Iterator<RegisterCommand> commands = rsfCreator.apply(register).getCommands();

            int commandCount = 0;
            try {
                while (commands.hasNext()) {
                    output.write(commands.next().serialise(commandParser).getBytes());

                    // TODO: is flushing every 10000 commands ok?
                    if (++commandCount >= 10000) {
                        output.flush();
                        commandCount = 0;
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

}
