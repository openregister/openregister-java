package uk.gov.indexer.ctserver;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties({"sha256_root_hash", "tree_head_signature"})
public class SignedTreeHead {
    @JsonProperty
    public int tree_size;

    @JsonProperty
    public long timestamp;
}
