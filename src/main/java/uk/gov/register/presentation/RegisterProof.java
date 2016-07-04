package uk.gov.register.presentation;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({"proof-identifier", "root-hash"})
public class RegisterProof {

    private static final String proofIdentifier = "merkle:sha-256";
    private final String rootHash;

    public RegisterProof(String rootHash) {
        this.rootHash = rootHash;
    }

    @SuppressWarnings("unused, used from template")
    @JsonProperty("proof-identifier")
    public String getProofIdentifier() {
        return proofIdentifier;
    }

    @SuppressWarnings("unused, used from template")
    @JsonProperty("root-hash")
    public String getRootHash() {
        return rootHash;
    }
}
