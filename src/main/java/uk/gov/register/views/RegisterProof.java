package uk.gov.register.views;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import uk.gov.register.util.HashValue;

@JsonPropertyOrder({"proof-identifier", "root-hash"})
public class RegisterProof {

    private static final String proofIdentifier = "merkle:sha-256";
    private final HashValue hash;
    private final int totalEntries;

    public RegisterProof(HashValue hashValue, int totalEntries) {
        this.hash = hashValue;
        this.totalEntries = totalEntries;
    }

    @SuppressWarnings("unused, used as jsonproperty")
    @JsonProperty("proof-identifier")
    public String getProofIdentifier() {
        return proofIdentifier;
    }

    @SuppressWarnings("unused, used as jsonproperty")
    @JsonProperty("root-hash")
    public HashValue getRootHash() {
        return hash;
    }

    @SuppressWarnings("unused, used as jsonproperty")
    @JsonProperty("total-entries")
    public int getTotalEntries() {
        return totalEntries;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RegisterProof that = (RegisterProof) o;

        if (totalEntries != that.totalEntries) return false;
        return hash.equals(that.hash);

    }

    @Override
    public int hashCode() {
        int result = hash.hashCode();
        result = 31 * result + totalEntries;
        return result;
    }
}
