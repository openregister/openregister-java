package uk.gov.register.proofs.ct;

public class SignedTreeHead {
    public final int tree_size;

    public final long timestamp;

    public final String sha256_root_hash;

    public final String tree_head_signature;

    public SignedTreeHead(int tree_size, long timestamp, String sha256_root_hash, String tree_head_signature) {
        this.tree_size = tree_size;
        this.timestamp = timestamp;
        this.sha256_root_hash = sha256_root_hash;
        this.tree_head_signature = tree_head_signature;
    }
}
