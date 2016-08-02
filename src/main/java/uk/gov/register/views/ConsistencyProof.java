package uk.gov.register.views;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.List;

@JsonPropertyOrder({"proof-identifier", "merkle-consistency-nodes"})
public class ConsistencyProof {

    private static final String proofIdentifier = "merkle:sha-256";
    private final List<String> consistencyNodes;

    public ConsistencyProof(List<String> consistencyNodes) {
        this.consistencyNodes = consistencyNodes;
    }

    @SuppressWarnings("unused, used as jsonproperty")
    @JsonProperty("proof-identifier")
    public String getProofIdentifier() {
        return proofIdentifier;
    }

    @SuppressWarnings("unused, used as jsonproperty")
    @JsonProperty("merkle-consistency-nodes")
    public List<String> getConsistencyNodes() {
        return consistencyNodes;
    }
}
