package uk.gov.register.service;

import uk.gov.register.core.RegisterContext;
import uk.gov.register.exceptions.RSFParseException;
import uk.gov.register.proofs.ProofGenerator;
import uk.gov.register.serialization.*;
import uk.gov.verifiablelog.VerifiableLog;

import javax.inject.Inject;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.function.Function;

public class RegisterSerialisationFormatService {
    private final RegisterContext registerContext;
    private final RSFExecutor rsfExecutor;
    private final RSFCreator rsfCreator;

    @Inject
    public RegisterSerialisationFormatService(RegisterContext registerContext, RSFExecutor rsfExecutor, RSFCreator rsfCreator) {
        this.registerContext = registerContext;
        this.rsfExecutor = rsfExecutor;
        this.rsfCreator = rsfCreator;
    }

    public void writeTo(OutputStream output, RSFFormatter rsfFormatter) {
        registerContext.transactionalRegisterOperation(register -> {
            Iterator<RegisterCommand> commands = registerContext.withV1VerifiableLog(verifiableLog -> {
                ProofGenerator proofGenerator = new ProofGenerator(verifiableLog);
                return rsfCreator.createV1(register, proofGenerator).getCommands();
            });

            writeCommands(commands, output, rsfFormatter);
        });
    }

    public void writeTo(OutputStream output, RSFFormatter rsfFormatter, int totalEntries1, int totalEntries2) {
        registerContext.transactionalRegisterOperation(register -> {
            Iterator<RegisterCommand> commands = registerContext.withV1VerifiableLog(verifiableLog -> {
                ProofGenerator proofGenerator = new ProofGenerator(verifiableLog);
                return rsfCreator.createV1(register, proofGenerator, totalEntries1, totalEntries2).getCommands();
            });

            writeCommands(commands, output, rsfFormatter);
        });
    }

    public void process(RegisterSerialisationFormat rsf) throws RSFParseException {
        Function<Function<VerifiableLog, Object>, Object> logMethod;

        if(rsf.getVersion() == RegisterSerialisationFormat.Version.V1) {
            logMethod = registerContext::withV1VerifiableLog;
        } else {
            logMethod = registerContext::withVerifiableLog;
        }

        registerContext.transactionalRegisterOperation(register -> {
            logMethod.apply(verifiableLog -> {
                ProofGenerator proofGenerator = new ProofGenerator(verifiableLog);
                rsfExecutor.execute(rsf, register, proofGenerator);

                return null;
            });

        });
    }

    public RegisterSerialisationFormat readFromV1(InputStream commandStream, RSFFormatter rsfFormatter) {
        BufferedReader buffer = new BufferedReader(new InputStreamReader(commandStream));
        Iterator<RegisterCommand> commandsIterator = buffer.lines()
                .map(rsfFormatter::parse)
                .iterator();
        return new RegisterSerialisationFormat(RegisterSerialisationFormat.Version.V1, commandsIterator);
    }

    public RegisterSerialisationFormat readFromV2(InputStream commandStream, RSFFormatter rsfFormatter) {
        BufferedReader buffer = new BufferedReader(new InputStreamReader(commandStream));
        Iterator<RegisterCommand> commandsIterator = buffer.lines()
                .map(rsfFormatter::parse)
                .iterator();
        return new RegisterSerialisationFormat(RegisterSerialisationFormat.Version.V2, commandsIterator);
    }

    private void writeCommands(Iterator<RegisterCommand> commands, OutputStream output, RSFFormatter rsfFormatter) {
        int commandCount = 0;
        try {
            while (commands.hasNext()) {
                output.write(rsfFormatter.format(commands.next()).getBytes());

                // TODO: is flushing every 10000 commands ok?
                if (++commandCount >= 10000) {
                    output.flush();
                    commandCount = 0;
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
