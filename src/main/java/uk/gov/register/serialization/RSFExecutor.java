package uk.gov.register.serialization;


import org.apache.commons.codec.digest.DigestUtils;
import uk.gov.register.core.HashingAlgorithm;
import uk.gov.register.core.Item;
import uk.gov.register.core.Register;
import uk.gov.register.util.HashValue;

import java.nio.charset.StandardCharsets;
import java.util.*;

public class RSFExecutor {

    private Map<String, CommandHandler> registeredHandlers;
    private Map<String, Integer> shaRefCount;

    public RSFExecutor() {
        registeredHandlers = new HashMap<>();
        shaRefCount = new HashMap<>();
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

            // this can be bettah
            errors.addAll(validate(command, register));

            if (errors.isEmpty()) {
                errors.addAll(execute(command, register));
            }
        }
        if (errors.isEmpty()) {
            errors.addAll(validateOrphanBits());
        }
        return errors;
    }


    private List<Exception> validate(RegisterCommand2 command, Register register) {
        // this ugly method won't be needed when we have symlinks
        // and won't have to rely on hashes
        List<Exception> result = new ArrayList<>();
        String commandName = command.getCommandName();
        if (commandName.equals("add-item")) {
            String hash = DigestUtils.sha256Hex(command.getCommandArguments().get(0).getBytes(StandardCharsets.UTF_8));
            shaRefCount.put(HashingAlgorithm.SHA256 + ":" + hash, 0);
        } else if (commandName.equals("append-entry")) {
            String sha256 = command.getCommandArguments().get(1);
            if (shaRefCount.containsKey(sha256)) {
                shaRefCount.put(sha256, shaRefCount.get(sha256) + 1);
            } else {
                HashValue hashValue = HashValue.decode(HashingAlgorithm.SHA256, sha256);
                Optional<Item> item = register.getItemBySha256(hashValue);
                if (!item.isPresent()) {
                    result.add(new Exception("OMG orphan append entry" + command.toString()));
                }
            }
        }

        return result;

    }

    private List<Exception> validateOrphanBits() {
        List<Exception> result = new ArrayList<>();
        shaRefCount.forEach((hash, count) -> {
            if (count < 1) {
                // this means it has not been referenced after it occurred
                result.add(new Exception("OMG orphan item, hash: " + hash));
            }
        });
        return result;
    }

    public void register(CommandHandler commandHandler) {
        registeredHandlers.put(commandHandler.getCommandName(), commandHandler);
    }

}