package uk.gov.register.presentation.view;

import com.fasterxml.jackson.annotation.JsonValue;
import uk.gov.register.presentation.RegisterData;
import uk.gov.register.presentation.RegisterDetail;

import java.time.Instant;

public class RegisterDetailView {
    private final int totalRecords;
    private final int totalEntries;
    private final int totalItems;
    private final Instant lastUpdated;
    private final RegisterData registerData;
    private final String registerDomain;

    public RegisterDetailView(
            int totalRecords,
            int totalEntries,
            int totalItems,
            Instant lastUpdated,
            RegisterData registerData,
            String registerDomain) {
        this.totalRecords = totalRecords;
        this.totalEntries = totalEntries;
        this.totalItems = totalItems;
        this.lastUpdated = lastUpdated;
        this.registerData = registerData;
        this.registerDomain = registerDomain;
    }

    @JsonValue
    public RegisterDetail getRegisterDetail() {
        return new RegisterDetail(
                registerDomain,
                totalRecords,
                totalEntries,
                totalItems,
                lastUpdated,
                registerData
        );
    }
}
