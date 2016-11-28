package uk.gov.register.views;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import uk.gov.register.util.HashValue;

import java.util.List;

@JsonPropertyOrder({"proof-identifier", "entry-number", "merkle-audit-path"})
public class EntryProof {
    private static final String proofIdentifier = "merkle:sha-256";
    private final String entryNumber;
    private final List<HashValue> auditPath;

    public EntryProof(String entryNumber, List<HashValue> auditPath) {
        this.entryNumber = entryNumber;
        this.auditPath = auditPath;
    }

    @SuppressWarnings("unused, used as jsonproperty")
    @JsonProperty("proof-identifier")
    public String getProofIdentifier() {
        return proofIdentifier;
    }

    @SuppressWarnings("unused, used as jsonproperty")
    @JsonProperty("entry-number")
    public String getEntryNumber() {
        return entryNumber;
    }

    @SuppressWarnings("unused, used as jsonproperty")
    @JsonProperty("merkle-audit-path")
    public List<HashValue> getAuditPath() {
        return auditPath;
    }
}