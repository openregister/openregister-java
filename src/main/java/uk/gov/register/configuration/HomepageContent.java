package uk.gov.register.configuration;

import java.util.List;
import java.util.Optional;

public class HomepageContent {
    private final Optional<String> registerHistoryUrl;
    private final Optional<String> custodianName;
    private final List<String> similarRegisters;
    private final List<String> indexes;

    public HomepageContent(Optional<String> registerHistoryUrl, Optional<String> custodianName, List<String> similarRegisters, List<String> indexes) {
        this.registerHistoryUrl = registerHistoryUrl;
        this.custodianName = custodianName;
        this.similarRegisters = similarRegisters;
        this.indexes = indexes;
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
    public String getTechDocsUrl() {
        return "https://registers-docs.cloudapps.digital";
    }

    @SuppressWarnings("unused, used from template")
    public Optional<String> getRegisterHistoryPageUrl() {
        return registerHistoryUrl;
    }

    public Optional<String> getCustodianName() { return custodianName; }

    public List<String> getSimilarRegisters() { return similarRegisters; }

    public List<String> getIndexes() { return indexes; }
}
