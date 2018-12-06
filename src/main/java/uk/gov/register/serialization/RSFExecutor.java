package uk.gov.register.serialization;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Splitter;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import uk.gov.register.core.Item;
import uk.gov.register.core.Register;
import uk.gov.register.exceptions.RSFParseException;
import uk.gov.register.proofs.ProofGenerator;
import uk.gov.register.util.HashValue;
import uk.gov.register.util.JsonToBlobHash;

import java.io.IOException;
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

    public void execute(RegisterSerialisationFormat rsf, Register register, ProofGenerator proofGenerator) throws RSFParseException {
        // HashValue and RSF file line number
        Map<HashValue, Integer> hashRefLine = new HashMap<>();
        Iterator<RegisterCommand> commands = rsf.getCommands();
        int rsfLine = 1;
        while (commands.hasNext()) {
            RegisterCommand command = commands.next();
            RegisterCommandContext context = new RegisterCommandContext(rsf, proofGenerator, rsfLine);

            validate(command, register, context, hashRefLine);
            execute(command, register, context);

            rsfLine++;
        }
        
        validateOrphanAddItems(hashRefLine);
    }

    private void execute(RegisterCommand command, Register register, RegisterCommandContext context) throws RSFParseException {
        if (registeredHandlers.containsKey(command.getCommandName())) {
            RegisterCommandHandler registerCommandHandler = registeredHandlers.get(command.getCommandName());
            registerCommandHandler.execute(command, register, context);
        } else {
            throw new RSFParseException("Handler not registered for command: " + command.getCommandName());
        }
    }

    private void validate(RegisterCommand command, Register register, RegisterCommandContext context, Map<HashValue, Integer> hashRefLine) throws RSFParseException {
        String commandName = command.getCommandName();
        if (commandName.equals("add-item")) {
            validateAddItem(command, context, hashRefLine);
        } else if (commandName.equals("append-entry")) {
            validateAppendEntry(command, context, register, hashRefLine);
        }
    }

    private void validateAppendEntry(RegisterCommand command, RegisterCommandContext context, Register register, Map<HashValue, Integer> hashRefLine) throws RSFParseException {
        String delimitedHashes = command.getCommandArguments().get(RSF_HASH_POSITION);
        if (StringUtils.isEmpty(delimitedHashes)) {
            return;
        }

        // FIXME: it should no longer be necessary to split here
        List<HashValue> hashes = Splitter.on(";").splitToList(delimitedHashes).stream()
                .map(s -> HashValue.decode(SHA256, s)).collect(toList());

        for (HashValue hashValue : hashes) {
            if (hashRefLine.containsKey(hashValue)) {
                hashRefLine.put(hashValue, 0);
            } else {
                Optional<Item> item;
                if(context.getVersion() == RegisterSerialisationFormat.Version.V1) {
                    item = register.getItemByV1Hash(hashValue);
                } else {
                    item = register.getItem(hashValue);
                }

                if (item.isPresent()) {
                    hashRefLine.put(hashValue, 0);
                } else {
                    throw new RSFParseException("Orphan append entry (line:" + context.getRsfLineNo() + "): " + command.toString());
                }
            }
        }
    }

    private void validateAddItem(RegisterCommand command, RegisterCommandContext context, Map<HashValue, Integer> hashRefLine) {
        String hash;

        if(context.getVersion() == RegisterSerialisationFormat.Version.V1) {
            hash = DigestUtils.sha256Hex(command.getCommandArguments().get(0).getBytes(StandardCharsets.UTF_8));
        } else {
            try {
                hash = JsonToBlobHash.apply(new ObjectMapper().readTree(command.getCommandArguments().get(0))).getValue();
            } catch (IOException e) {
                throw new RSFParseException("Blob is not valid JSON: (line:" + context.getRsfLineNo() + "): " + command.toString());
            }
        }

        hashRefLine.put(new HashValue(SHA256, hash), context.getRsfLineNo());
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
