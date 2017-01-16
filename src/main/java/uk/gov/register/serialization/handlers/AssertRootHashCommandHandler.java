package uk.gov.register.serialization.handlers;

import uk.gov.register.core.HashingAlgorithm;
import uk.gov.register.core.Register;
import uk.gov.register.serialization.RSFResult;
import uk.gov.register.serialization.RegisterCommand;
import uk.gov.register.serialization.RegisterCommandHandler;
import uk.gov.register.util.HashValue;
import uk.gov.register.views.RegisterProof;

import java.security.NoSuchAlgorithmException;

public class AssertRootHashCommandHandler extends RegisterCommandHandler {
    @Override
    protected RSFResult executeCommand(RegisterCommand command, Register register) {
        try {
            HashValue hash = HashValue.decode(HashingAlgorithm.SHA256, command.getCommandArguments().get(0));
            RegisterProof expectedProof = new RegisterProof(hash);
            RegisterProof actualProof = register.getRegisterProof();
            if (!actualProof.equals(expectedProof)) {
                String message = String.format("Root hashes don't match. Expected: %s actual: %s", expectedProof.getRootHash().toString(), actualProof.getRootHash().toString());
                return RSFResult.createFailResult(message);
            }
            return RSFResult.createSuccessResult();
        } catch (NoSuchAlgorithmException e) {
            return RSFResult.createFailResult("Exception when executing command: " + command, e);
        }
    }

    @Override
    public String getCommandName() {
        return "assert-root-hash";
    }
}
