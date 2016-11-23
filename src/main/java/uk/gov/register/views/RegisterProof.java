package uk.gov.register.views;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import uk.gov.register.core.HashingAlgorithm;

import java.util.Objects;

@JsonPropertyOrder({"proof-identifier", "root-hash"})
public class RegisterProof {

    private static final String proofIdentifier = "merkle:sha-256";
    private final String rootHash;

    public RegisterProof(String rootHash) {
        this.rootHash = HashingAlgorithm.SHA256.toString() + ":" + rootHash;
    }

    @SuppressWarnings("unused, used as jsonproperty")
    @JsonProperty("proof-identifier")
    public String getProofIdentifier() {
        return proofIdentifier;
    }

    @SuppressWarnings("unused, used as jsonproperty")
    @JsonProperty("root-hash")
    public String getRootHash() {
        return rootHash;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RegisterProof registerProof = (RegisterProof) o;
        return Objects.equals(registerProof.getRootHash(), this.getRootHash());
    }

    @Override
    public int hashCode() {
        return 31 * getRootHash().hashCode();
    }
}
