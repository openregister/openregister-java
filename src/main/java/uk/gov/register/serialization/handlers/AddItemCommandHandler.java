package uk.gov.register.serialization.handlers;

import uk.gov.register.core.Item;
import uk.gov.register.core.Register;
import uk.gov.register.serialization.RSFResult;
import uk.gov.register.serialization.RegisterCommand;
import uk.gov.register.serialization.RegisterCommandHandler;
import uk.gov.register.util.ObjectReconstructor;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class AddItemCommandHandler extends RegisterCommandHandler {


    private final ObjectReconstructor objectReconstructor;

    public AddItemCommandHandler() {
        objectReconstructor = new ObjectReconstructor();

    }

    @Override
    protected RSFResult executeCommand(RegisterCommand command, Register register) {
        try {
            String jsonContent = command.getCommandArguments().get(0);
            Item item = new Item(objectReconstructor.reconstruct(jsonContent));
            register.putItem(item);
            return RSFResult.createSuccessResult();
        } catch (IOException e) {
            return RSFResult.createFailResult("Exception when executing command: " + command, e);
        }
    }

    @Override
    public String getCommandName() {
        return "add-item";
    }
}
