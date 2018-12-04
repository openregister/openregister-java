package uk.gov.register.serialization.handlers;

import uk.gov.register.core.Entry;
import uk.gov.register.core.EntryType;
import uk.gov.register.core.Item;
import uk.gov.register.core.Register;
import uk.gov.register.exceptions.RSFParseException;
import uk.gov.register.proofs.ProofGenerator;
import uk.gov.register.serialization.RSFFormatter;
import uk.gov.register.serialization.RegisterCommand;
import uk.gov.register.serialization.RegisterCommandHandler;
import uk.gov.register.util.HashValue;

import java.time.Instant;
import java.util.List;

import static uk.gov.register.core.HashingAlgorithm.SHA256;

public class AppendEntryCommandHandler extends RegisterCommandHandler {
    @Override
    protected void executeCommand(RegisterCommand command, Register register, ProofGenerator proofGenerator) {
        try {
            List<String> parts = command.getCommandArguments();
            HashValue itemHash = HashValue.decode(SHA256, parts.get(RSFFormatter.RSF_HASH_POSITION));
            HashValue blobHash = register.getItemByV1Hash(itemHash).map(Item::getBlobHash)
                    .orElseThrow(() -> new RSFParseException("Item not found for hash " + itemHash.getValue()));
            EntryType entryType = EntryType.valueOf(parts.get(RSFFormatter.RSF_ENTRY_TYPE_POSITION));
            int newEntryNo = register.getTotalEntries(entryType) + 1;
            Entry entry = new Entry(newEntryNo, itemHash, blobHash, Instant.parse(parts.get(RSFFormatter.RSF_TIMESTAMP_POSITION)), parts.get(RSFFormatter.RSF_KEY_POSITION), entryType);
            register.appendEntry(entry);
        } catch(RSFParseException e) {
            throw e;
        }
        catch (Exception e) {
            throw new RSFParseException("Exception when executing command: " + command, e);
        }
    }

    @Override
    public String getCommandName() {
        return "append-entry";
    }
}
