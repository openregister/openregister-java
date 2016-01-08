package uk.gov.indexer.ctserver;

import java.nio.ByteBuffer;

public class MerkleTreeLeaf {
    private String leaf_input;
    private String extra_data;

    public String getLeaf_input() {
        return leaf_input;
    }

    public void setLeaf_input(String leaf_input) {
        this.leaf_input = leaf_input;
    }

    public String getExtra_data() {
        return extra_data;
    }

    public void setExtra_data(String extra_data) {
        this.extra_data = extra_data;
    }
}
