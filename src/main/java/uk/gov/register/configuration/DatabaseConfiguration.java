package uk.gov.register.configuration;

import io.dropwizard.db.DataSourceFactory;
import org.jvnet.hk2.annotations.Contract;

@Contract
public interface DatabaseConfiguration {
    DataSourceFactory getDatabase();
}
