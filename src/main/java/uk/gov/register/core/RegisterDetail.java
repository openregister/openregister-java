package uk.gov.register.core;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.register.util.ISODateFormatter;

import java.time.Instant;
import java.util.Optional;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class RegisterDetail {
    private final String domain;
    private final int totalRecords;
    private final int totalEntries;
    private final Optional<Instant> lastUpdated;
    private RegisterMetadata registerMetadata;

    public RegisterDetail(
            String domain,
            int totalRecords,
            int totalEntries,
            Optional<Instant> lastUpdated,
            RegisterMetadata registerMetadata) {
        this.domain = domain;
        this.totalRecords = totalRecords;
        this.totalEntries = totalEntries;
        this.lastUpdated = lastUpdated;
        this.registerMetadata = registerMetadata;
    }

    @JsonProperty("domain")
    public String getDomain() {
        return domain;
    }

    @SuppressWarnings("unused, used from template")
    @JsonProperty("last-updated")
    public String getLastUpdatedTime() {
        return lastUpdated.isPresent() ? ISODateFormatter.format(lastUpdated.get()) : null;
    }

    @SuppressWarnings("unused, used to serialize in register json")
    @JsonProperty("register-record")
    public RegisterMetadata getRegisterMetadata() {
        return registerMetadata;
    }

    @SuppressWarnings("unused, used from template")
    @JsonProperty("total-entries")
    public int getTotalEntries() {
        return totalEntries;
    }

    @SuppressWarnings("unused, used from template")
    @JsonProperty("total-records")
    public int getTotalRecords() {
        return totalRecords;
    }
}
