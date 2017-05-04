package uk.gov.register.configuration;

import uk.gov.register.indexer.function.CurrentCountriesIndexFunction;
import uk.gov.register.indexer.function.IndexFunction;
import uk.gov.register.indexer.function.LocalAuthorityByTypeIndexFunction;

import java.util.Arrays;
import java.util.Set;

import static java.util.stream.Collectors.toSet;

public enum IndexFunctionConfiguration {

    CURRENT_COUNTRIES(Constants.CURRENT_COUNTRIES, new CurrentCountriesIndexFunction(Constants.CURRENT_COUNTRIES)),
    LOCAL_AUTHORITY_BY_TYPE(Constants.LOCAL_AUTHORITY_BY_TYPE, new LocalAuthorityByTypeIndexFunction(Constants.LOCAL_AUTHORITY_BY_TYPE));

    public static IndexFunctionConfiguration getValueLowerCase(String name) {
        try {
            return IndexFunctionConfiguration.valueOf(name.toUpperCase().replaceAll("-", "_"));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(String.format("Error: no index function corresponding to index: %s in config.yaml", name), e);
        }
    }

    private String name;
    private Set<IndexFunction> indexFunctions;

    IndexFunctionConfiguration(String name, IndexFunction... indexFunctions) {
        this.name = name;
        this.indexFunctions = Arrays.stream(indexFunctions).collect(toSet());
    }

    public String getName() {
        return name;
    }

    public Set<IndexFunction> getIndexFunctions() {
        return indexFunctions;
    }

    private static class Constants {
        private static final String CURRENT_COUNTRIES = "current-countries";
        private static final String LOCAL_AUTHORITY_BY_TYPE = "local-authority-by-type";
    }

}
