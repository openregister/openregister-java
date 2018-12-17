package uk.gov.register.views.v2;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import uk.gov.register.util.HashValue;

@JsonPropertyOrder({"root-hash", "total-entries"})
public class RootHashView {
    private final HashValue hash;
    private final int totalEntries;

    public RootHashView(HashValue hashValue, int totalEntries) {
        this.hash = hashValue;
        this.totalEntries = totalEntries;
    }

    @SuppressWarnings("unused, used as jsonproperty")
    @JsonProperty("root-hash")
    public String getRootHash() {
        return hash.multihash();
    }

    @SuppressWarnings("unused, used as jsonproperty")
    @JsonProperty("total-entries")
    public int getTotalEntries() {
        return totalEntries;
    }
}
