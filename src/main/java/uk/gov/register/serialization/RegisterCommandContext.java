package uk.gov.register.serialization;

import uk.gov.register.core.Register;
import uk.gov.register.proofs.ProofGenerator;

public class RegisterCommandContext {
    private final RegisterSerialisationFormat rsf;
    private final ProofGenerator proofGenerator;

    public RegisterCommandContext(RegisterSerialisationFormat rsf, ProofGenerator proofGenerator) {
        this.rsf = rsf;
        this.proofGenerator = proofGenerator;
    }

    public RegisterSerialisationFormat.Version getVersion() {
        return rsf.getVersion();
    }

    public ProofGenerator getProofGenerator() {
        return proofGenerator;
    }
}
