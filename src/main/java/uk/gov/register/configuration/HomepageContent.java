package uk.gov.register.configuration;

import java.util.List;
import java.util.Optional;

public class HomepageContent {
    private final List<String> indexes;

    public HomepageContent(List<String> indexes) {
        this.indexes = indexes;
    }

    @SuppressWarnings("unused, used from template")
    public String getRegistersIntroductionUrl() {
        return "https://registers.cloudapps.digital";
    }

    @SuppressWarnings("unused, used from template")
    public String getTechDocsUrl() {
        return "https://registers-docs.cloudapps.digital";
    }

    public List<String> getIndexes() { return indexes; }
}
