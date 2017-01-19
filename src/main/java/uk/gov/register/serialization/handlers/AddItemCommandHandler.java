package uk.gov.register.serialization.handlers;

import uk.gov.register.core.Item;
import uk.gov.register.core.Register;
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
    protected RegisterResult executeCommand(RegisterCommand command, Register register) {
        try {
            String jsonContent = command.getCommandArguments().get(0);
            Item item = new Item(objectReconstructor.reconstruct(jsonContent));
            register.putItem(item);
            return RegisterResult.createSuccessResult();
        } catch (Exception e) {
            return RegisterResult.createFailResult("Exception when executing command: " + command, e);
        }
    }

    @Override
    public String getCommandName() {
        return "add-item";
    }
}
