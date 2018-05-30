package uk.gov.register.serialization;

import com.google.common.base.Splitter;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import uk.gov.register.core.Item;
import uk.gov.register.core.Register;
import uk.gov.register.exceptions.RSFParseException;
import uk.gov.register.util.HashValue;

import java.nio.charset.StandardCharsets;
import java.util.*;

import static java.util.stream.Collectors.toList;
import static uk.gov.register.core.HashingAlgorithm.SHA256;

public class RSFExecutor {
    public static final int RSF_HASH_POSITION = 3;

    private Map<String, RegisterCommandHandler> registeredHandlers;

    public RSFExecutor() {
        registeredHandlers = new HashMap<>();
    }

    public void register(RegisterCommandHandler registerCommandHandler) {
        registeredHandlers.put(registerCommandHandler.getCommandName(), registerCommandHandler);
    }

    public void execute(RegisterSerialisationFormat rsf, Register register) throws RSFParseException {
        // HashValue and RSF file line number
        Map<HashValue, Integer> hashRefLine = new HashMap<>();
        Iterator<RegisterCommand> commands = rsf.getCommands();
        int rsfLine = 1;
        while (commands.hasNext()) {
            RegisterCommand command = commands.next();

            validate(command, register, rsfLine, hashRefLine);
            execute(command, register);

            rsfLine++;
        }
        
        validateOrphanAddItems(hashRefLine);
    }

    private void execute(RegisterCommand command, Register register) throws RSFParseException {
        if (registeredHandlers.containsKey(command.getCommandName())) {
            RegisterCommandHandler registerCommandHandler = registeredHandlers.get(command.getCommandName());
            registerCommandHandler.execute(command, register);
        } else {
            throw new RSFParseException("Handler not registered for command: " + command.getCommandName());
        }
    }

    private void validate(RegisterCommand command, Register register, int rsfLine, Map<HashValue, Integer> hashRefLine) throws RSFParseException {
        // this ugly method won't be needed when we have symlinks
        // and won't have to rely on hashes

        String commandName = command.getCommandName();
        if (commandName.equals("add-item")) {
            validateAddItem(command, rsfLine, hashRefLine);
        } else if (commandName.equals("append-entry")) {
            validateAppendEntry(command, rsfLine, register, hashRefLine);
        }
    }

    private void validateAppendEntry(RegisterCommand command, int rsfLine, Register register, Map<HashValue, Integer> hashRefLine) throws RSFParseException {
        String delimitedHashes = command.getCommandArguments().get(RSF_HASH_POSITION);
        if (StringUtils.isEmpty(delimitedHashes)) {
            return;
        }
        
        List<HashValue> hashes = Splitter.on(";").splitToList(delimitedHashes).stream()
                .map(s -> HashValue.decode(SHA256, s)).collect(toList());

        for (HashValue hashValue : hashes) {
            if (hashRefLine.containsKey(hashValue)) {
                hashRefLine.put(hashValue, 0);
            } else {
                Optional<Item> item = register.getItem(hashValue);
                if (!item.isPresent()) {
                    throw new RSFParseException("Orphan append entry (line:" + rsfLine + "): " + command.toString());
                } else {
                    hashRefLine.put(hashValue, 0);
                }
            }
        }
    }

    private void validateAddItem(RegisterCommand command, int rsfLine, Map<HashValue, Integer> hashRefLine) {
        String hash = DigestUtils.sha256Hex(command.getCommandArguments().get(0).getBytes(StandardCharsets.UTF_8));
        hashRefLine.put(new HashValue(SHA256, hash), rsfLine);
    }

    private void validateOrphanAddItems(Map<HashValue, Integer> hashRefLine) throws RSFParseException {
        List<String> orphanItems = new ArrayList<>();
        hashRefLine.forEach((hash, rsfLine) -> {
            if (rsfLine > 0) {
                orphanItems.add("Orphan add item (line:" + rsfLine + "): " + hash.encode());
            }
        });

        if (!orphanItems.isEmpty()) {
            throw new RSFParseException(String.join("; ", orphanItems));
        }
    }
}
