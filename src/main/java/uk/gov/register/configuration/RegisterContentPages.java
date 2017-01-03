package uk.gov.register.configuration;

import java.util.Optional;

public class RegisterContentPages {
    private final Optional<String> registerHistoryUrl;
    private final Optional<String> custodianName;

    public RegisterContentPages(Optional<String> registerHistoryUrl, Optional<String> custodianName) {
        this.registerHistoryUrl = registerHistoryUrl;
        this.custodianName = custodianName;
    }

    @SuppressWarnings("unused, used from template")
    public String getRegistersIntroductionUrl() {
        return "https://www.gov.uk/government/publications/registers/registers";
    }

    @SuppressWarnings("unused, used from template")
    public String getUsingRegistersGuidanceUrl() {
        return "https://www.gov.uk/guidance/using-registers-to-build-a-service";
    }

    @SuppressWarnings("unused, used from template")
    public String getSpecificationUrl() {
        return "http://openregister.github.io/specification/";
    }

    @SuppressWarnings("unused, used from template")
    public Optional<String> getRegisterHistoryPageUrl() {
        return registerHistoryUrl;
    }

    public Optional<String> getCustodianName() { return custodianName; }
}
