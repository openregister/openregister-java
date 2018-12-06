package uk.gov.register.serialization.handlers;

import uk.gov.register.core.Item;
import uk.gov.register.core.Register;
import uk.gov.register.exceptions.RSFParseException;
import uk.gov.register.proofs.ProofGenerator;
import uk.gov.register.serialization.RSFFormatter;
import uk.gov.register.serialization.RegisterResult;
import uk.gov.register.serialization.RegisterCommand;
import uk.gov.register.serialization.RegisterCommandHandler;
import uk.gov.register.util.ObjectReconstructor;

public class AddItemCommandHandler extends RegisterCommandHandler {
    private final ObjectReconstructor objectReconstructor;

    public AddItemCommandHandler() {
        objectReconstructor = new ObjectReconstructor();
    }

    @Override
    protected void executeCommand(RegisterCommand command, Register register, ProofGenerator proofGenerator) {
        try {
            String jsonContent = command.getCommandArguments().get(RSFFormatter.RSF_ITEM_ARGUMENT_POSITION);
            Item item = new Item(objectReconstructor.reconstruct(jsonContent));
            register.addItem(item);
        } catch (Exception e) {
            throw new RSFParseException("Exception when executing command: " + command, e);
        }
    }

    @Override
    public String getCommandName() {
        return "add-item";
    }
}
