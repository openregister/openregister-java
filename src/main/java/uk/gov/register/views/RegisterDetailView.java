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
    private Optional<String> custodianName;

    public RegisterDetailView(
            int totalRecords,
            int totalEntries,
            Optional<Instant> lastUpdated,
            RegisterMetadata registerMetadata,
            String registerDomain,
            Optional<String> custodianName) {
        this.totalRecords = totalRecords;
        this.totalEntries = totalEntries;
        this.lastUpdated = lastUpdated;
        this.registerMetadata = registerMetadata;
        this.registerDomain = registerDomain;
        this.custodianName = custodianName;
    }

    @JsonValue
    public RegisterDetail getRegisterDetail() {
        return new RegisterDetail(
                registerDomain,
                totalRecords,
                totalEntries,
                lastUpdated,
                registerMetadata,
                custodianName);
    }
}
