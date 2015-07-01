package uk.gov.register.presentation;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
import org.hibernate.validator.constraints.NotEmpty;

public class PresentationConfiguration extends Configuration implements ZookeeperConfiguration {
    @NotEmpty
    @JsonProperty
    private String zookeeperServer;

    public String getZookeeperServer() {
        return zookeeperServer;
    }
}
