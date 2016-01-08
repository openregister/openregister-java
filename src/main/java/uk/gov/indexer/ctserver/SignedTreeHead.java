package uk.gov.indexer.ctserver;

public class SignedTreeHead {
    private int tree_size;
    private long timestamp;
    private String sha256_root_hash;
    private String tree_head_signature;

    public int getTree_size() {
        return tree_size;
    }

    public void setTree_size(int tree_size) {
        this.tree_size = tree_size;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getSha256_root_hash() {
        return sha256_root_hash;
    }

    public void setSha256_root_hash(String sha256_root_hash) {
        this.sha256_root_hash = sha256_root_hash;
    }

    public String getTree_head_signature() {
        return tree_head_signature;
    }

    public void setTree_head_signature(String tree_head_signature) {
        this.tree_head_signature = tree_head_signature;
    }
}
