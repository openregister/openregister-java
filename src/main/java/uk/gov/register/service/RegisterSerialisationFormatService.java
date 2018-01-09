package uk.gov.register.service;

import com.amazonaws.auth.EnvironmentVariableCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sns.model.PublishResult;
import uk.gov.register.core.Register;
import uk.gov.register.core.RegisterContext;
import uk.gov.register.serialization.*;
import uk.gov.register.serialization.aws.message.RegisterUpdateMessage;

import javax.inject.Inject;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    public void writeTo(OutputStream output, RSFFormatter RSFFormatter, String indexName) {
        writeTo(output, RSFFormatter, register -> rsfCreator.create(register, indexName));
    }

    public void writeTo(OutputStream output, RSFFormatter RSFFormatter, String indexName, int totalEntries1, int totalEntries2) {
        writeTo(output, RSFFormatter, register -> rsfCreator.create(register, indexName, totalEntries1, totalEntries2));
    }

    public RegisterResult process(RegisterSerialisationFormat rsf) {
        final Register[] modifiedRegister = new Register[1];
        final RegisterResult registerResult = registerContext.transactionalRegisterOperation(register -> {
            modifiedRegister[0] = register;
            return rsfExecutor.execute(rsf, register);
        });

        if (registerResult.isSuccessful()) {
            prepareAndSendMessage(rsf, modifiedRegister[0]);
        }

        return registerResult;
    }

    private void prepareAndSendMessage(final RegisterSerialisationFormat rsf, final Register register) {
        final StringBuilder message = new StringBuilder();

        rsf.getCommands().forEach(c ->
                message.append(c.getOriginal()).append("\n")
        );

        this.sendMessage(new RegisterUpdateMessage(register.getRegisterName().value(), message.toString()));
    }

    private void sendMessage(final RegisterUpdateMessage registerUpdateMessage) {
        final String topicArn = "arn:aws:sns:eu-west-1:022990953738:registers-all-updates"; // 00x00 Dynamic???
        final AmazonSNSClient snsClient = new AmazonSNSClient(new EnvironmentVariableCredentialsProvider()); // 00x00 Other method for cloud
        final PublishRequest publishRequest;
        final PublishResult publishResult;

        snsClient.setRegion(Region.getRegion(Regions.EU_WEST_1));

        publishRequest = new PublishRequest(topicArn, registerUpdateMessage.toJson());
        publishResult = snsClient.publish(publishRequest);

        if (Objects.isNull(publishResult.getMessageId())) {
            // 00x00: Deal with errors
        }
    }

    public RegisterSerialisationFormat readFrom(InputStream commandStream, RSFFormatter rsfFormatter) {
        final BufferedReader buffer = new BufferedReader(new InputStreamReader(commandStream));
        final List<RegisterCommand> registerCommandStream = buffer.lines()
                .map(rsfFormatter::parse)
                .collect(Collectors.toList());

        return new RegisterSerialisationFormat(registerCommandStream);
    }

    private void writeTo(OutputStream output, RSFFormatter rsfFormatter, Function<Register, RegisterSerialisationFormat> rsfCreatorFunc) {
        registerContext.transactionalRegisterOperation(register -> {
            Iterator<RegisterCommand> commands = rsfCreatorFunc.apply(register).getCommands().iterator();

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
        });
    }
}
