package uk.gov.register.serialization;

import org.apache.commons.codec.digest.DigestUtils;
import uk.gov.register.core.HashingAlgorithm;
import uk.gov.register.core.Item;
import uk.gov.register.core.Register;
import uk.gov.register.util.HashValue;

import java.nio.charset.StandardCharsets;
import java.util.*;

public class RSFExecutor {

    private Map<String, RegisterCommandHandler> registeredHandlers;


    public RSFExecutor() {
        registeredHandlers = new HashMap<>();
    }

    public void register(RegisterCommandHandler registerCommandHandler) {
        registeredHandlers.put(registerCommandHandler.getCommandName(), registerCommandHandler);
    }

    public RegisterResult execute(RegisterSerialisationFormat rsf, Register register) {
        // HashValue and RSF file line number
        Map<HashValue, Integer> hashRefLine = new HashMap<>();
        Iterator<RegisterCommand> commands = rsf.getCommands();
        int rsfLine = 1;
        while (commands.hasNext()) {
            RegisterCommand command = commands.next();

            RegisterResult validationResult = validate(command, register, rsfLine, hashRefLine);
            if (!validationResult.isSuccessful()) {
                return validationResult;
            }

            RegisterResult executionResult = execute(command, register);
            if (!executionResult.isSuccessful()) {
                return executionResult;
            }

            rsfLine++;
        }

        return validateOrphanAddItems(hashRefLine);
    }

    private RegisterResult execute(RegisterCommand command, Register register) {
        if (registeredHandlers.containsKey(command.getCommandName())) {
            RegisterCommandHandler registerCommandHandler = registeredHandlers.get(command.getCommandName());
            return registerCommandHandler.execute(command, register);
        } else {
            return RegisterResult.createFailResult("Handler not registered for command: " + command.getCommandName());
        }
    }

    private RegisterResult validate(RegisterCommand command, Register register, int rsfLine, Map<HashValue, Integer> hashRefLine) {
        // this ugly method won't be needed when we have symlinks
        // and won't have to rely on hashes

        String commandName = command.getCommandName();
        if (commandName.equals("add-item")) {
            validateAddItem(command, rsfLine, hashRefLine);
        } else if (commandName.equals("append-entry")) {
            if (validateAppendEntry(command, register, hashRefLine))
                return RegisterResult.createFailResult("Orphan append entry (line:" + rsfLine + "): " + command.toString());
        }
        return RegisterResult.createSuccessResult();
    }

    private Boolean validateAppendEntry(RegisterCommand command, Register register, Map<HashValue, Integer> hashRefLine) {
        String entrySha256 = command.getCommandArguments().get(1);
        HashValue hashValue = HashValue.decode(HashingAlgorithm.SHA256, entrySha256);
        if (hashRefLine.containsKey(hashValue)) {
            hashRefLine.put(hashValue, 0);
        } else {
            Optional<Item> item = register.getItemBySha256(hashValue);
            if (!item.isPresent()) {
                return true;
            }
        }
        return false;
    }

    private void validateAddItem(RegisterCommand command, int rsfLine, Map<HashValue, Integer> hashRefLine) {
        String hash = DigestUtils.sha256Hex(command.getCommandArguments().get(0).getBytes(StandardCharsets.UTF_8));
        hashRefLine.put(new HashValue(HashingAlgorithm.SHA256, hash), rsfLine);
    }

    private RegisterResult validateOrphanAddItems(Map<HashValue, Integer> hashRefLine) {
        List<String> orphanItems = new ArrayList<>();
        hashRefLine.forEach((hash, rsfLine) -> {
            if (rsfLine > 0) {
                orphanItems.add("Orphan add item (line:" + rsfLine + "): " + hash.encode());
            }
        });

        if (orphanItems.isEmpty()) {
            return RegisterResult.createSuccessResult();
        } else {
            return RegisterResult.createFailResult(String.join("; ", orphanItems));
        }
    }
}