package uk.gov.register.serialization;

import uk.gov.register.core.Item;
import uk.gov.register.core.Register;
import uk.gov.register.util.ObjectReconstructor;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class AddItemCommandHandler extends CommandHandler {


    private final ObjectReconstructor objectReconstructor;

    public AddItemCommandHandler() {
        objectReconstructor = new ObjectReconstructor();

    }

    @Override
    protected List<Exception> executeCommand(RegisterCommand2 command, Register register) {
        try {
            String jsonContent = command.getCommandArguments().get(0);
            Item item = new Item(objectReconstructor.reconstruct(jsonContent));
            register.putItem(item);
            return Collections.emptyList();
        } catch (IOException e) {
            e.printStackTrace();
            return Collections.singletonList(e);
        }
    }

    @Override
    public String getCommandName() {
        return "add-item";
    }
}
