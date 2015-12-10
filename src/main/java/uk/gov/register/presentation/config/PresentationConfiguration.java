package uk.gov.register.presentation.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
import io.dropwizard.client.HttpClientConfiguration;
import io.dropwizard.client.JerseyClientConfiguration;
import io.dropwizard.db.DataSourceFactory;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public class PresentationConfiguration extends Configuration {
    @Valid
    @NotNull
    private JerseyClientConfiguration jerseyClient = new JerseyClientConfiguration();

    @Valid
    @NotNull
    @JsonProperty
    private DataSourceFactory database;

    public DataSourceFactory getDatabase() {
        return database;
    }

    public JerseyClientConfiguration getJerseyClientConfiguration() {
        return jerseyClient;
    }

}
