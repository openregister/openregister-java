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

    public RSFResult execute(RegisterSerialisationFormat rsf, Register register) {
        // this instead of integer can be full command
        // then when it's referenced just empty the value
        // when checking for orphan there should not be any hash with command as those
        // should have been emptied
        // ?? memory consumption here ??

        // for now hash and line from file?
        // hash and rsf file line number
        Map<String, Integer> shaRefLine = new HashMap<>();
        Iterator<RegisterCommand> commands = rsf.getCommands();
        int rsfLine = 1;
        while (commands.hasNext()) {
            RegisterCommand command = commands.next();

            RSFResult validationResult = validate(command, register, rsfLine, shaRefLine);
            if (!validationResult.isSuccessful()) {
                return validationResult;
            }

            RSFResult executionResult = execute(command, register);
            if (!executionResult.isSuccessful()) {
                return executionResult;
            }

            rsfLine++;
        }

        return validateOrphanItems(shaRefLine);
    }

    private RSFResult execute(RegisterCommand command, Register register) {
        if (registeredHandlers.containsKey(command.getCommandName())) {
            RegisterCommandHandler registerCommandHandler = registeredHandlers.get(command.getCommandName());
            return registerCommandHandler.execute(command, register);
        } else {
            return RSFResult.createFailResult("Handler not registered for command: " + command.getCommandName());
        }
    }

    private RSFResult validate(RegisterCommand command, Register register, int rsfLine, Map<String, Integer> shaRefLine) {
        // this ugly method won't be needed when we have symlinks
        // and won't have to rely on hashes

        String commandName = command.getCommandName();
        if (commandName.equals("add-item")) {
            String hash = DigestUtils.sha256Hex(command.getCommandArguments().get(0).getBytes(StandardCharsets.UTF_8));
            shaRefLine.put(HashingAlgorithm.SHA256 + ":" + hash, rsfLine);
        } else if (commandName.equals("append-entry")) {
            String sha256 = command.getCommandArguments().get(1);
            if (shaRefLine.containsKey(sha256)) {
                shaRefLine.put(sha256, 0);
            } else {
                HashValue hashValue = HashValue.decode(HashingAlgorithm.SHA256, sha256);
                Optional<Item> item = register.getItemBySha256(hashValue);
                if (!item.isPresent()) {
                    RSFResult.createFailResult("Orphan append entry" + command.toString());
                }
            }
        }
        return RSFResult.createSuccessResult();
    }

    private RSFResult validateOrphanItems(Map<String, Integer> shaRefLine) {
        List<String> orphanItems = new ArrayList<>();
        shaRefLine.forEach((hash, rsfLine) -> {
            if (rsfLine > 0) {
                orphanItems.add("Orphan item, hash: " + hash + ", line: " + rsfLine);
            }
        });

        if (orphanItems.isEmpty()) {
            return RSFResult.createSuccessResult();
        } else {
            return RSFResult.createFailResult(String.join("; ", orphanItems));
        }
    }
}