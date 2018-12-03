package uk.gov.register.serialization.handlers;

import uk.gov.register.core.HashingAlgorithm;
import uk.gov.register.core.Register;
import uk.gov.register.exceptions.AssertRootHashException;
import uk.gov.register.exceptions.RSFParseException;
import uk.gov.register.serialization.RSFFormatter;
import uk.gov.register.serialization.RegisterCommand;
import uk.gov.register.serialization.RegisterCommandHandler;
import uk.gov.register.serialization.RegisterResult;
import uk.gov.register.util.HashValue;

public class AssertRootHashCommandHandler extends RegisterCommandHandler {
    @Override
    protected void executeCommand(RegisterCommand command, Register register) {
        try {
            HashValue expectedHash = HashValue.decode(HashingAlgorithm.SHA256, command.getCommandArguments().get(RSFFormatter.RSF_ASSERT_ROOT_HASH_ARGUMENT_POSITION));
            HashValue actualHash = register.getV1RegisterProof().getRootHash();
            if (!actualHash.equals(expectedHash)) {
                throw new AssertRootHashException(expectedHash, actualHash);
            }
        } catch (Exception e) {
            throw new RSFParseException("Exception when executing command: " + command, e);
        }
    }

    @Override
    public String getCommandName() {
        return "assert-root-hash";
    }
}
