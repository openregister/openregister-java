package uk.gov.register.serialization.handlers;

import uk.gov.register.core.HashingAlgorithm;
import uk.gov.register.core.Register;
import uk.gov.register.serialization.RegisterResult;
import uk.gov.register.serialization.RegisterCommand;
import uk.gov.register.serialization.RegisterCommandHandler;
import uk.gov.register.util.HashValue;
import uk.gov.register.views.RegisterProof;

public class AssertRootHashCommandHandler extends RegisterCommandHandler {
    @Override
    protected RegisterResult executeCommand(RegisterCommand command, Register register) {
        try {
            HashValue hash = HashValue.decode(HashingAlgorithm.SHA256, command.getCommandArguments().get(0));
            RegisterProof expectedProof = new RegisterProof(hash);
            RegisterProof actualProof = register.getRegisterProof();
            if (!actualProof.equals(expectedProof)) {
                String message = String.format("Root hashes don't match. Expected: %s actual: %s", expectedProof.getRootHash().toString(), actualProof.getRootHash().toString());
                return RegisterResult.createFailResult(message);
            }
            return RegisterResult.createSuccessResult();
        } catch (Exception e) {
            return RegisterResult.createFailResult("Exception when executing command: " + command, e);
        }
    }

    @Override
    public String getCommandName() {
        return "assert-root-hash";
    }
}
