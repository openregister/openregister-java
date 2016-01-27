package uk.gov.register.presentation;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class RegisterDetail {
    private final static DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("d MMM uuuu");

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
        return DATE_TIME_FORMATTER.format(lastUpdated);
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
