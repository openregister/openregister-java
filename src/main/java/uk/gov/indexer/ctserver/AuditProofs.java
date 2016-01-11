package uk.gov.indexer.ctserver;

import java.util.List;

public class AuditProofs {
    private int leaf_index;
    private List<String> audit_path;

    public int getLeaf_index() {
        return leaf_index;
    }

    public void setLeaf_index(int leaf_index) {
        this.leaf_index = leaf_index;
    }

    public List<String> getAudit_path() {
        return audit_path;
    }

    public void setAudit_path(List<String> audit_path) {
        this.audit_path = audit_path;
    }
}
