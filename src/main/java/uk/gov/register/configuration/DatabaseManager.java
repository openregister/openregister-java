package uk.gov.register.configuration;

import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.db.ManagedDataSource;
import io.dropwizard.jdbi.DBIFactory;
import io.dropwizard.setup.Environment;
import org.skife.jdbi.v2.DBI;

public class DatabaseManager {
    private final DataSourceFactory database;
    private final ManagedDataSource dataSource;
    private final DBI dbi;

    public DatabaseManager(DatabaseConfiguration databaseConfiguration, Environment environment, DBIFactory dbiFactory) {
        this.database = databaseConfiguration.getDatabase();
        this.database.getProperties().put("ApplicationName", "openregister_java");

        // dbiFactory.build() will ensure that this dataSource is correctly shut down
        // it will also be shared with flyway
        this.dataSource = database.build(environment.metrics(), "openregister_java");
        this.dbi = dbiFactory.build(environment, database, dataSource, "openregister_java");
    }

    public ManagedDataSource getDataSource() {
        return dataSource;
    }

    public DBI getDbi() {
        return dbi;
    }
}
