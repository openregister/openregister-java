package uk.gov.indexer.ctserver;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SignedTreeHead {
    @JsonProperty
    private int tree_size;

    @JsonProperty
    private long timestamp;

    @JsonProperty
    private String sha256_root_hash;

    @JsonProperty
    private String tree_head_signature;

    @SuppressWarnings("unused, required to create object by json deserilizer")
    public SignedTreeHead() {
    }

    public SignedTreeHead(int tree_size, long timestamp, String sha256_root_hash, String tree_head_signature) {
        this.tree_size = tree_size;
        this.timestamp = timestamp;
        this.sha256_root_hash = sha256_root_hash;
        this.tree_head_signature = tree_head_signature;
    }

    public int getTree_size() {
        return tree_size;
    }

    public long getTimestamp() {
        return timestamp;
    }

    @SuppressWarnings("unused, used by DAO")
    public String getSha256_root_hash() {
        return sha256_root_hash;
    }

    @SuppressWarnings("unused, used by DAO")
    public String getTree_head_signature() {
        return tree_head_signature;
    }
}
