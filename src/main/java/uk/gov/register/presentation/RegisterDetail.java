package uk.gov.register.presentation;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

public class RegisterDetail {

    private final String domain;
    private final int totalRecords;
    private final int totalEntries;
    private final int totalItems;
    private final LocalDateTime lastUpdated;
    private final EntryView entryView;

    public RegisterDetail(
            String domain,
            int totalRecords,
            int totalEntries,
            int totalItems,
            LocalDateTime lastUpdated,
            EntryView entryView) {
        this.domain = domain;
        this.totalRecords = totalRecords;
        this.totalEntries = totalEntries;
        this.totalItems = totalItems;
        this.lastUpdated = lastUpdated;
        this.entryView = entryView;
    }

    @JsonProperty("domain")
    public String getDomain() {
        return domain;
    }

    @JsonProperty("last-updated")
    public String getLastUpdatedTime(){
        return lastUpdated.toString();
    }

    @JsonProperty("record")
    public EntryView getEntry() {
        return entryView;
    }

    @JsonProperty("total-entries")
    public int getTotalEntries() {
        return totalEntries;
    }

    @JsonProperty("total-items")
    public int getTotalItems() {
        return totalItems;
    }

    @JsonProperty("total-records")
    public int getTotalRecords(){
        return totalRecords;
    }
}
