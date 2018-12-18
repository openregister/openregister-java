package uk.gov.register.views.v2;

import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.register.util.HashValue;

import java.util.List;
import java.util.stream.Collectors;

public class AuditPathView {
    private final List<HashValue> auditPath;

    public AuditPathView(List<HashValue> auditPath) {
        this.auditPath = auditPath;
    }

    @JsonProperty("audit-path")
    List<String> getAuditPath() {
        return auditPath.stream().map(HashValue::multihash).collect(Collectors.toList());
    }
}
