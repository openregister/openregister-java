package uk.gov.register.presentation.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
import io.dropwizard.db.DataSourceFactory;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public class PresentationConfiguration extends Configuration implements ZookeeperConfiguration {
    @Valid
    @NotNull
    @JsonProperty
    private DataSourceFactory database;

    @NotEmpty
    @JsonProperty
    private String zookeeperServer;

    @NotEmpty
    @JsonProperty
    private String name;

    public DataSourceFactory getDatabase() {
        return database;
    }

    public String getZookeeperServer() {
        return zookeeperServer;
    }

    public String getName() {
        return name;
    }
}
