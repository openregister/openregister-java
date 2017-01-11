package uk.gov.register.serialization;


import uk.gov.register.core.Register;

import java.util.*;

public class RSFExecutor {

    private Map<String, CommandHandler> registeredHandlers;

    public RSFExecutor() {
        registeredHandlers = new HashMap<>();
    }

    private List<Exception> execute(RegisterCommand2 command, Register register) {
        if (registeredHandlers.containsKey(command.getCommandName())) {
            CommandHandler commandHandler = registeredHandlers.get(command.getCommandName());
            return commandHandler.execute(command, register);
        } else {
            return Arrays.asList(new Exception("Handler not registered for command: " + command.getCommandName()));
        }
    }

    public List<Exception> execute(RegisterSerialisationFormat rsf, Register register) {
        List<Exception> errors = new ArrayList<>();
        Iterator<RegisterCommand2> commands = rsf.getCommands2();
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