package uk.gov.register.views;

import com.fasterxml.jackson.annotation.JsonValue;
import uk.gov.register.core.RegisterDetail;
import uk.gov.register.core.RegisterMetadata;

import java.time.Instant;
import java.util.Optional;

public class RegisterDetailView {
    private final int totalRecords;
    private final int totalEntries;
    private final Optional<Instant> lastUpdated;
    private final RegisterMetadata registerMetadata;
    private final String registerDomain;

    public RegisterDetailView(
            int totalRecords,
            int totalEntries,
            Optional<Instant> lastUpdated,
            RegisterMetadata registerMetadata,
            String registerDomain) {
        this.totalRecords = totalRecords;
        this.totalEntries = totalEntries;
        this.lastUpdated = lastUpdated;
        this.registerMetadata = registerMetadata;
        this.registerDomain = registerDomain;
    }

    @JsonValue
    public RegisterDetail getRegisterDetail() {
        return new RegisterDetail(
                registerDomain,
                totalRecords,
                totalEntries,
                lastUpdated,
                registerMetadata
        );
    }
}
