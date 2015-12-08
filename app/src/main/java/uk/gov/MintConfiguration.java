package uk.gov;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
import io.dropwizard.db.DataSourceFactory;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public class MintConfiguration extends Configuration {
    @SuppressWarnings("unused")
    @Valid
    @NotNull
    @JsonProperty
    private DataSourceFactory database;

    @SuppressWarnings("unused")
    @Valid
    @NotNull
    @JsonProperty
    private String register;

    public DataSourceFactory getDatabase() {
        return database;
    }

    public String getRegister() {
        return register;
    }
}
