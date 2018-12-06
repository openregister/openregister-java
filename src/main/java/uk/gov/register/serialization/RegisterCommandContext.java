package uk.gov.register.serialization;

import uk.gov.register.proofs.ProofGenerator;

public class RegisterCommandContext {
    private final RegisterSerialisationFormat rsf;
    private final ProofGenerator proofGenerator;
    private final int rsfLineNo;

    public RegisterCommandContext(RegisterSerialisationFormat rsf, ProofGenerator proofGenerator, int rsfLineNo) {
        this.rsf = rsf;
        this.proofGenerator = proofGenerator;
        this.rsfLineNo = rsfLineNo;
    }

    public RegisterSerialisationFormat.Version getVersion() {
        return rsf.getVersion();
    }

    public ProofGenerator getProofGenerator() {
        return proofGenerator;
    }

    public int getRsfLineNo() {
        return rsfLineNo;
    }
}
