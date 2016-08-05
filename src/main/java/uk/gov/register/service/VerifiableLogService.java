package uk.gov.register.service;

import uk.gov.register.db.EntryMerkleLeafStore;
import uk.gov.register.db.EntryQueryDAO;
import uk.gov.register.views.ConsistencyProof;
import uk.gov.register.views.EntryProof;
import uk.gov.register.views.RegisterProof;
import uk.gov.verifiablelog.VerifiableLog;
import uk.gov.verifiablelog.store.memoization.MemoizationStore;

import javax.inject.Inject;
import javax.xml.bind.DatatypeConverter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.stream.Collectors;

public class VerifiableLogService {
    private final EntryQueryDAO entryDAO;
    private final MemoizationStore memoizationStore;
    private final VerifiableLog verifiableLog;

    @Inject
    public VerifiableLogService(EntryQueryDAO entryDAO, MemoizationStore memoizationStore) throws NoSuchAlgorithmException {
        this.entryDAO = entryDAO;
        this.memoizationStore = memoizationStore;
        this.verifiableLog = createVerifiableLog(entryDAO);
    }

    public RegisterProof getRegisterProof() throws NoSuchAlgorithmException {
        try {
            entryDAO.begin();
            String rootHash = bytesToString(verifiableLog.currentRoot());
            return new RegisterProof(rootHash);
        } finally {
            entryDAO.rollback();
        }
    }

    public EntryProof getEntryProof(int entryNumber, int totalEntries){
        try {
            entryDAO.begin();

            List<String> auditProof = verifiableLog.auditProof(entryNumber, totalEntries).stream()
                    .map(this::bytesToString).collect(Collectors.toList());

            return new EntryProof(Integer.toString(entryNumber), auditProof);
        } finally {
            entryDAO.rollback();
        }
    }

    public ConsistencyProof getConsistencyProof(int totalEntries1, int totalEntries2){
        try {
            entryDAO.begin();
            List<String> consistencyProof = verifiableLog.consistencyProof(totalEntries1, totalEntries2).stream()
                    .map(this::bytesToString).collect(Collectors.toList());

            return new ConsistencyProof(consistencyProof);
        } finally {
            entryDAO.rollback();
        }
    }

    private VerifiableLog createVerifiableLog(EntryQueryDAO entryDAO) throws NoSuchAlgorithmException {
        return new VerifiableLog(MessageDigest.getInstance("SHA-256"), new EntryMerkleLeafStore(entryDAO), memoizationStore);
    }

    private String bytesToString(byte[] bytes) {
        return DatatypeConverter.printHexBinary(bytes).toLowerCase();
    }


}
