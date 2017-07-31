package uk.gov.register.configuration;

import uk.gov.register.core.EntryType;
import uk.gov.register.indexer.function.CurrentCountriesIndexFunction;
import uk.gov.register.indexer.function.IndexFunction;
import uk.gov.register.indexer.function.LocalAuthorityByTypeIndexFunction;
import uk.gov.register.indexer.function.LatestByKeyIndexFunction;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toSet;

public enum IndexFunctionConfiguration {

    CURRENT_COUNTRIES(IndexNames.CURRENT_COUNTRIES, EntryType.user, new CurrentCountriesIndexFunction(IndexNames.CURRENT_COUNTRIES)),
    LOCAL_AUTHORITY_BY_TYPE(IndexNames.LOCAL_AUTHORITY_BY_TYPE, EntryType.user, new LocalAuthorityByTypeIndexFunction(IndexNames.LOCAL_AUTHORITY_BY_TYPE)),
    METADATA(IndexNames.METADATA, EntryType.system, new LatestByKeyIndexFunction(IndexNames.METADATA));

    public static List<IndexFunctionConfiguration> getConfigurations(List<String> indexNames) {
        List<IndexFunctionConfiguration> configurations = indexNames.stream().map(n -> getValueLowerCase(n)).collect(Collectors.toList());
        configurations.addAll(getDefaultConfigurations());
        return configurations;
    }

    private static IndexFunctionConfiguration getValueLowerCase(String name) {
        try {
            return IndexFunctionConfiguration.valueOf(name.toUpperCase().replaceAll("-", "_"));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(String.format("Error: no index function corresponding to index: %s in config.yaml", name), e);
        }
    }

    private static List<IndexFunctionConfiguration> getDefaultConfigurations() {
        return Arrays.asList(METADATA);
    }

    private String name;
    private EntryType entryType;
    private Set<IndexFunction> indexFunctions;

    IndexFunctionConfiguration(String name, EntryType entryType, IndexFunction... indexFunctions) {
        this.name = name;
        this.entryType = entryType;
        this.indexFunctions = Arrays.stream(indexFunctions).collect(toSet());
    }

    public String getName() {
        return name;
    }

    public EntryType getEntryType() {
        return entryType;
    }

    public Set<IndexFunction> getIndexFunctions() {
        return indexFunctions;
    }

    public static class IndexNames {
        public static final String CURRENT_COUNTRIES = "current-countries";
        public static final String LOCAL_AUTHORITY_BY_TYPE = "local-authority-by-type";
        public static final String METADATA = "metadata";
    }

}
