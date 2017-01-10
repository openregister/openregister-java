package uk.gov.register.serialization;


import uk.gov.register.core.Register;

import java.util.*;

public class CommandExecutor {

    private Map<String, CommandHandler> registeredHandlers;

    public CommandExecutor() {
        registeredHandlers = new HashMap<>();
    }

    public List<Exception> execute(RegisterCommand2 command, Register register) {
        if (registeredHandlers.containsKey(command.getCommandName())) {
            CommandHandler commandHandler = registeredHandlers.get(command.getCommandName());
            return commandHandler.execute(command, register);
        } else {
            return Arrays.asList(new Exception("Handler not registered for command: " + command.getCommandName()));
        }
    }

    public List<Exception> execute(Iterator<RegisterCommand2> commands, Register register) {
        List<Exception> errors = new ArrayList<>();
        while (commands.hasNext() && errors.isEmpty()) {
            RegisterCommand2 command = commands.next();
            errors.addAll(execute(command, register));
        }
        return errors;
    }

    public void register(CommandHandler commandHandler) {
        registeredHandlers.put(commandHandler.getCommandName(), commandHandler);
    }

}