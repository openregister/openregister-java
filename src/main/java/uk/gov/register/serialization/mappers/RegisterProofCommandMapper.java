package uk.gov.register.serialization.mappers;

import uk.gov.register.serialization.RegisterCommand;
import uk.gov.register.serialization.RegisterCommandMapper;
import uk.gov.register.views.RegisterProof;

import java.util.Collections;

public class RegisterProofCommandMapper extends RegisterCommandMapper<RegisterProof,RegisterCommand> {

    @Override
    public RegisterCommand apply(RegisterProof registerProof) {
        return new RegisterCommand("assert-root-hash", Collections.singletonList(registerProof.getRootHash().encode()));
    }
}
