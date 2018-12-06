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

    public EntryProof getEntryProof(int entryNumber, int totalEntries) {
        List<HashValue> auditProof = verifiableLog.auditProof(entryNumber - 1, totalEntries)
                        .stream()
                        .map(hashBytes -> new HashValue(HashingAlgorithm.SHA256, bytesToString(hashBytes)))
                        .collect(Collectors.toList());

        return new EntryProof(Integer.toString(entryNumber), auditProof);
    }

    public ConsistencyProof getConsistencyProof(int totalEntries1, int totalEntries2) {
        List<HashValue> consistencyProof = verifiableLog.consistencyProof(totalEntries1, totalEntries2)
                        .stream()
                        .map(hashBytes -> new HashValue(HashingAlgorithm.SHA256, bytesToString(hashBytes)))
                        .collect(Collectors.toList());

        return new ConsistencyProof(consistencyProof);
    }

    private String bytesToString(byte[] bytes) {
        return DatatypeConverter.printHexBinary(bytes).toLowerCase();
    }
}
