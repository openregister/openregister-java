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

    public DataSourceFactory getDatabase() {
        System.out.println(database.getUrl());
        return database;
    }

    public String getZookeeperServer() {
        System.out.println(zookeeperServer);
        return zookeeperServer;
    }
}
