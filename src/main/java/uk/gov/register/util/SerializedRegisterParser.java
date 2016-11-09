package uk.gov.register.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.register.serialization.CommandParser;
import uk.gov.register.serialization.RegisterCommand;
import uk.gov.register.serialization.RegisterComponents;

import java.io.*;
import java.util.List;
import java.util.stream.Collectors;

public class SerializedRegisterParser {

    private static final Logger LOG = LoggerFactory.getLogger(SerializedRegisterParser.class);

    public RegisterComponents parseCommands(InputStream commandStream) {
        BufferedReader buffer = new BufferedReader(new InputStreamReader(commandStream));

        List<RegisterCommand> commands = buffer.lines().map(s -> new CommandParser().newCommand(s)).collect(Collectors.toList());
        // don't close the reader as the caller will close the input stream
        return new RegisterComponents(commands);

    }


}
