package uk.gov.register.configuration;

import java.util.List;
import java.util.Optional;

public class HomepageContent {
    private final List<String> similarRegisters;
    private final List<String> indexes;

    public HomepageContent(List<String> similarRegisters, List<String> indexes) {
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

    public List<String> getSimilarRegisters() { return similarRegisters; }

    public List<String> getIndexes() { return indexes; }
}
