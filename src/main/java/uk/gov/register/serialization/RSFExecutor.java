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
    private Map<String, Integer> shaRefCount;

    public RSFExecutor() {
        registeredHandlers = new HashMap<>();
        shaRefCount = new HashMap<>();
    }

    public void register(RegisterCommandHandler registerCommandHandler) {
        registeredHandlers.put(registerCommandHandler.getCommandName(), registerCommandHandler);
    }

    public RSFResult execute(RegisterSerialisationFormat rsf, Register register) {
        Iterator<RegisterCommand> commands = rsf.getCommands();
        while (commands.hasNext()) {
            RegisterCommand command = commands.next();

            RSFResult validationResult = validate(command, register);
            if (!validationResult.isSuccessful()) {
                return validationResult;
            }

            RSFResult executionResult = execute(command, register);
            if (!executionResult.isSuccessful()) {
                return executionResult;
            }
        }

        return validateOrphanItems();
    }

    private RSFResult execute(RegisterCommand command, Register register) {
        if (registeredHandlers.containsKey(command.getCommandName())) {
            RegisterCommandHandler registerCommandHandler = registeredHandlers.get(command.getCommandName());
            return registerCommandHandler.execute(command, register);
        } else {
            return RSFResult.createFailResult("Handler not registered for command: " + command.getCommandName());
        }
    }

    private RSFResult validate(RegisterCommand command, Register register) {
        // this ugly method won't be needed when we have symlinks
        // and won't have to rely on hashes

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
                    RSFResult.createFailResult("OMG orphan append entry" + command.toString());
                }
            }
        }
        return RSFResult.createSuccessResult();
    }

    private RSFResult validateOrphanItems() {
        List<String> orphanItems = new ArrayList<>();
        shaRefCount.forEach((hash, count) -> {
            if (count < 1) {
                orphanItems.add("OMG orphan item, hash: " + hash);
            }
        });

        if (orphanItems.isEmpty()) {
            return RSFResult.createSuccessResult();
        } else {
            return RSFResult.createFailResult(String.join("\n", orphanItems));
        }
    }


}