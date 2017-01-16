package uk.gov.register.service;

import com.google.common.base.Strings;
import uk.gov.register.core.Register;
import uk.gov.register.core.RegisterContext;
import uk.gov.register.serialization.*;

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
        writeTo(output, rsfFormatter, rsfCreator::create);
    }

    public void writeTo(OutputStream output, RSFFormatter RSFFormatter, int totalEntries1, int totalEntries2) {
        writeTo(output, RSFFormatter, register -> rsfCreator.create(register, totalEntries1, totalEntries2));
    }

    public RSFResult processRegisterSerialisationFormat(RegisterSerialisationFormat rsf) {
        final RSFResult[] rsfResult = new RSFResult[1];
        registerContext.transactionalRegisterOperation(register -> {
            RSFResult executionResult = rsfExecutor.execute(rsf, register);
            rsfResult[0] = executionResult;
            return executionResult.isSuccessful();
        });
        return rsfResult[0];
    }

    public RegisterSerialisationFormat readFrom(InputStream commandStream, RSFFormatter rsfFormatter) {
        BufferedReader buffer = new BufferedReader(new InputStreamReader(commandStream));
        Iterator<RegisterCommand> commandsIterator = buffer.lines()
                .filter(str -> !Strings.isNullOrEmpty(str))
                .map(rsfFormatter::parse)
                .iterator();
        return new RegisterSerialisationFormat(commandsIterator);
    }

    private void writeTo(OutputStream output, RSFFormatter rsfFormatter, Function<Register, RegisterSerialisationFormat> rsfCreatorFunc) {
        registerContext.transactionalRegisterOperation(register -> {
            Iterator<RegisterCommand> commands = rsfCreatorFunc.apply(register).getCommands();

            int commandCount = 0;
            try {
                while (commands.hasNext()) {
                    output.write(rsfFormatter.format(commands.next()).getBytes());

                    // TODO: is flushing every 10000 commands ok?
                    if (++commandCount >= 1) {
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
