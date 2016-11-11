package uk.gov.register.serialization;

import uk.gov.register.core.Register;
import uk.gov.register.exceptions.RootHashAssertionException;
import uk.gov.register.views.RegisterProof;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public class AssertRootHashCommand extends RegisterCommand {

    private RegisterProof registerProof;

    public AssertRootHashCommand(RegisterProof registerProof) {
        this.registerProof = registerProof;
    }

    @Override
    public void execute(Register register, AtomicInteger entryNumber) throws Exception {
        RegisterProof actualProof = register.getRegisterProof();
        if (actualProof != this.registerProof) {
            throw new RootHashAssertionException("Actual root hash: "+actualProof.getRootHash()+" does not match expected: "+this.registerProof.getRootHash());
        }

    }

    @Override
    public String serialise(CommandParser commandParser) {
        return commandParser.serialise(registerProof);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AssertRootHashCommand command = (AssertRootHashCommand) o;
        return Objects.equals(command.registerProof.getRootHash(), this.registerProof.getRootHash());
    }

    @Override
    public int hashCode() {
        return 31 * registerProof.getRootHash().hashCode();
    }
}
