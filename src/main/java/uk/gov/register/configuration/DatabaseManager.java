package uk.gov.register.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.db.ManagedDataSource;
import io.dropwizard.jdbi.DBIFactory;
import io.dropwizard.setup.Environment;
import org.skife.jdbi.v2.DBI;

import java.io.IOException;

public class DatabaseManager {
    private final DataSourceFactory database;
    private final ManagedDataSource dataSource;
    private final DBI dbi;

    public DatabaseManager(DatabaseConfiguration databaseConfiguration, Environment environment, DBIFactory dbiFactory, Boolean isRunningOnCloudFoundry) throws IOException {
        this.database = databaseConfiguration.getDatabase();
        this.database.getProperties().put("ApplicationName", "openregister_java");

        if (isRunningOnCloudFoundry) {
            this.database.setUrl(getCloudFoundryDatabaseServiceJDBCUrl());
        }

        // dbiFactory.build() will ensure that this dataSource is correctly shut down
        // it will also be shared with flyway
        this.dataSource = database.build(environment.metrics(), "openregister_java");
        this.dbi = dbiFactory.build(environment, database, dataSource, "openregister_java");
    }

    private String getCloudFoundryDatabaseServiceJDBCUrl() throws IOException {
        return new ObjectMapper()
                .readTree(System.getenv("VCAP_SERVICES"))
                .at("/postgres/0/credentials/jdbcuri")
                .textValue() + "&ssl=true";
    }

    public ManagedDataSource getDataSource() {
        return dataSource;
    }

    public DBI getDbi() {
        return dbi;
    }
}
