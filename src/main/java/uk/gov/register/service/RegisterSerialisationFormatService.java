package uk.gov.register.service;

import org.apache.jena.ext.com.google.common.collect.Iterators;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.register.core.HashingAlgorithm;
import uk.gov.register.core.Register;
import uk.gov.register.core.RegisterContext;
import uk.gov.register.serialization.*;
import uk.gov.register.util.HashValue;
import uk.gov.register.views.RegisterProof;

import javax.inject.Inject;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;


public class RegisterSerialisationFormatService {

    private final String EMPTY_ROOT_HASH = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855";
    private final RegisterProof emptyRegisterProof;

    private final RegisterContext registerContext;
    private RSFExecutor RSFExecutor;

    private static final Logger LOG = LoggerFactory.getLogger(RegisterSerialisationFormatService.class);


    @Inject
    public RegisterSerialisationFormatService(RegisterContext registerContext, RSFExecutor RSFExecutor) {
        this.registerContext = registerContext;
        this.RSFExecutor = RSFExecutor;
        this.emptyRegisterProof = new RegisterProof(new HashValue(HashingAlgorithm.SHA256, EMPTY_ROOT_HASH));
    }


    public void writeTo(OutputStream output, RSFFormat RSFFormat) {
        writeTo(output, RSFFormat, this::createRegisterSerialisationFormat);
    }

    public void writeTo(OutputStream output, RSFFormat RSFFormat, int totalEntries1, int totalEntries2) {
        writeTo(output, RSFFormat, register -> createRegisterSerialisationFormat(register, totalEntries1, totalEntries2));
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

    public List<Exception> processRegisterSerialisationFormat(RegisterSerialisationFormat rsf) {
        List<Exception> results = new ArrayList<>();
        registerContext.transactionalRegisterOperation(register -> {

            try {

                LOG.debug("before executing commands");

                List<Exception> cmResults = RSFExecutor.execute(rsf, register);

                results.addAll(cmResults);

                LOG.debug("after executing commands");

            } catch (Exception e) {
                results.add(e);
            }

            if (!results.isEmpty()) {
                throw new RuntimeException(results.get(0));
//                throw new RuntimeException("RSF processing failed with exceptions");
            }
        });
        return results;
    }

    public RegisterSerialisationFormat readFrom(InputStream commandStream, RSFFormat rsfFormat) {
        LOG.debug("reading commands");

        BufferedReader buffer = new BufferedReader(new InputStreamReader(commandStream));
        LOG.debug("pass the buffer");

        Stream<String> lines = buffer.lines();
        LOG.debug("pass the buffer.lines()");

        Iterator<RegisterCommand2> commandsIterator = lines.map(line -> {
            LOG.debug("-------- lazy converting :) -------");
            LOG.debug(line);
            return rsfFormat.parse(line);
        }).filter(i -> i != null).iterator();


        LOG.debug("pass the conversion");

//        Iterator<RegisterCommand> commands = parser.getCommands();
        LOG.debug("finished reading commands");
        // don't close the reader as the caller will close the input stream
        RegisterSerialisationFormat rsf = new RegisterSerialisationFormat(null, commandsIterator);
//        processRegisterSerialisationFormat(rsf);
        LOG.debug("pass processing");
        return rsf;

    }

    private void writeTo(OutputStream output, RSFFormat RSFFormat, Function<Register, RegisterSerialisationFormat> rsfCreator) {
        registerContext.transactionalRegisterOperation(register -> {
            Iterator<RegisterCommand> commands = rsfCreator.apply(register).getCommands();

            int commandCount = 0;
            try {
                while (commands.hasNext()) {
                    output.write(commands.next().serialise(RSFFormat).getBytes());

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
