package uk.gov.register.presentation.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
import io.dropwizard.db.DataSourceFactory;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public class PresentationConfiguration extends Configuration {
    @Valid
    @NotNull
    @JsonProperty
    private DataSourceFactory database;

    public DataSourceFactory getDatabase() {
        System.out.println(database.getUrl());
        return database;
    }
}
