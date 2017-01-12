package uk.gov.register.serialization.handlers;

import uk.gov.register.core.HashingAlgorithm;
import uk.gov.register.core.Register;
import uk.gov.register.serialization.RegisterCommand;
import uk.gov.register.serialization.RegisterCommandHandler;
import uk.gov.register.util.HashValue;
import uk.gov.register.views.RegisterProof;

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AssertRootHashCommandHandler extends RegisterCommandHandler {
    @Override
    protected List<Exception> executeCommand(RegisterCommand command, Register register) {
        try {
            HashValue hash = HashValue.decode(HashingAlgorithm.SHA256, command.getCommandArguments().get(0));
            RegisterProof expectedProof = new RegisterProof(hash);
            RegisterProof actualProof = register.getRegisterProof();
            List<Exception> result = new ArrayList<>();
            if (!actualProof.equals(expectedProof)) {
                result.add(new Exception("Root hashes don't match. Expected: " +
                        expectedProof.getRootHash().toString() + " actual: " + actualProof.getRootHash().toString()));
            }
            return result;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return Collections.singletonList(e);
        }
    }

    @Override
    public String getCommandName() {
        return "assert-root-hash";
    }
}
