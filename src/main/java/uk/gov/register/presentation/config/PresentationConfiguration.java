package uk.gov.register.presentation.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
import io.dropwizard.db.DataSourceFactory;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.Map;

public class PresentationConfiguration extends Configuration implements FieldsConfiguration {
    @Valid
    @NotNull
    @JsonProperty
    private DataSourceFactory database;

    @Valid
    @NotNull
    @JsonProperty
    private Map<String, FieldConfiguration> fields;

    public DataSourceFactory getDatabase() {
        return database;
    }

    @Override
    public Map<String, FieldConfiguration> getFields() {
        return fields;
    }
}
