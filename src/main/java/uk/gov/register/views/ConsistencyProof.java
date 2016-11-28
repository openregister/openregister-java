package uk.gov.register.views;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import uk.gov.register.core.HashingAlgorithm;
import uk.gov.register.util.HashValue;

import java.util.List;
import java.util.stream.Collectors;

@JsonPropertyOrder({"proof-identifier", "merkle-consistency-nodes"})
public class ConsistencyProof {

    private static final String proofIdentifier = "merkle:" + HashingAlgorithm.SHA256.toString();
    private final List<HashValue> consistencyNodes;

    public ConsistencyProof(List<HashValue> consistencyNodes) {
        this.consistencyNodes = consistencyNodes;
    }

    @SuppressWarnings("unused, used as jsonproperty")
    @JsonProperty("proof-identifier")
    public String getProofIdentifier() {
        return proofIdentifier;
    }

    @SuppressWarnings("unused, used as jsonproperty")
    @JsonProperty("merkle-consistency-nodes")
    public List<HashValue> getConsistencyNodes() {
        List<String> nodes = consistencyNodes.stream().map(p -> p.toString()).collect(Collectors.toList());

        return consistencyNodes;
    }
}
