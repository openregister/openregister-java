package uk.gov.register.presentation.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
import io.dropwizard.client.JerseyClientConfiguration;
import io.dropwizard.db.DataSourceFactory;
import uk.gov.organisation.client.GovukClientConfiguration;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.net.URI;

public class PresentationConfiguration extends Configuration implements GovukClientConfiguration, RegisterDomainConfiguration, ResourceConfiguration {
    @Valid
    @NotNull
    @JsonProperty
    private JerseyClientConfiguration jerseyClient = new JerseyClientConfiguration();

    @Valid
    @NotNull
    @JsonProperty
    private DataSourceFactory database;

    @Valid
    @NotNull
    @JsonProperty
    private String registerDomain = "openregister.org";

    @Valid
    @JsonProperty
    private boolean enableDownloadResource = false;

    @Override
    public String getRegisterDomain() {
        return registerDomain;
    }

    public DataSourceFactory getDatabase() {
        return database;
    }

    public JerseyClientConfiguration getJerseyClientConfiguration() {
        return jerseyClient;
    }

    @Override
    public URI getGovukEndpoint() {
        return URI.create("https://www.gov.uk");
    }

    @Override
    public boolean getEnableDownloadResource() {
        return enableDownloadResource;
    }
}
