package uk.gov.indexer.ctserver;

import java.util.List;

public class Entries {
    public List<MerkleTreeLeaf> entries;

    public List<MerkleTreeLeaf> getEntries() {
        return entries;
    }

    public void setEntries(List<MerkleTreeLeaf> entries) {
        this.entries = entries;
    }
}
