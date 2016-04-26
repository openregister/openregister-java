package uk.gov.register.presentation;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;

public class RegisterDetail {
    private final String domain;
    private final int totalRecords;
    private final int totalEntries;
    private final int totalItems;
    private final Instant lastUpdated;
    private RegisterData registerData;

    public RegisterDetail(
            String domain,
            int totalRecords,
            int totalEntries,
            int totalItems,
            Instant lastUpdated,
            RegisterData registerData) {
        this.domain = domain;
        this.totalRecords = totalRecords;
        this.totalEntries = totalEntries;
        this.totalItems = totalItems;
        this.lastUpdated = lastUpdated;
        this.registerData = registerData;
    }

    @JsonProperty("domain")
    public String getDomain() {
        return domain;
    }

    @SuppressWarnings("unused, used from template")
    @JsonProperty("last-updated")
    public String getLastUpdatedTime() {
        return ISODateFormatter.format(lastUpdated);
    }

    @SuppressWarnings("unused, used to serialize in register json")
    @JsonProperty("register-record")
    public RegisterData getRegisterData() {
        return registerData;
    }

    @SuppressWarnings("unused, used from template")
    @JsonProperty("total-entries")
    public int getTotalEntries() {
        return totalEntries;
    }

    @SuppressWarnings("unused, used from template")
    @JsonProperty("total-items")
    public int getTotalItems() {
        return totalItems;
    }

    @SuppressWarnings("unused, used from template")
    @JsonProperty("total-records")
    public int getTotalRecords() {
        return totalRecords;
    }
}
