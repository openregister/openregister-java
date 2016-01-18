package uk.gov.indexer.ctserver;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties({"extra_data"})
public class MerkleTreeLeaf {
    @JsonProperty
    public String leaf_input;
}
