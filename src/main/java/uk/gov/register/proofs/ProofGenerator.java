package uk.gov.register.proofs;

import uk.gov.register.core.HashingAlgorithm;
import uk.gov.register.util.HashValue;
import uk.gov.register.views.ConsistencyProof;
import uk.gov.register.views.EntryProof;
import uk.gov.register.views.RegisterProof;
import uk.gov.verifiablelog.VerifiableLog;

import javax.xml.bind.DatatypeConverter;
import java.util.List;
import java.util.stream.Collectors;

public class ProofGenerator {
    private final VerifiableLog verifiableLog;

    public ProofGenerator(VerifiableLog verifiableLog) {
        this.verifiableLog = verifiableLog;
    }

    public RegisterProof getRegisterProof(int totalEntries) {
        return new RegisterProof(getRootHash(totalEntries), totalEntries);
    }

    public HashValue getRootHash(int totalEntries) {
        return new HashValue(HashingAlgorithm.SHA256, bytesToString(verifiableLog.getSpecificRootHash(totalEntries)));
    }

    public HashValue getRootHash() {
        return new HashValue(HashingAlgorithm.SHA256, bytesToString(verifiableLog.getCurrentRootHash()));
    }

    public List<HashValue> getLeafAuditPath(int entryNumber, int totalEntries) {
        return verifiableLog.auditProof(entryNumber - 1, totalEntries)
                        .stream()
                        .map(hashBytes -> new HashValue(HashingAlgorithm.SHA256, bytesToString(hashBytes)))
                        .collect(Collectors.toList());
    }

    public List<HashValue> getAuditPath(int totalEntries1, int totalEntries2) {
         return verifiableLog.consistencyProof(totalEntries1, totalEntries2)
                        .stream()
                        .map(hashBytes -> new HashValue(HashingAlgorithm.SHA256, bytesToString(hashBytes)))
                        .collect(Collectors.toList());
    }

    private String bytesToString(byte[] bytes) {
        return DatatypeConverter.printHexBinary(bytes).toLowerCase();
    }
}
