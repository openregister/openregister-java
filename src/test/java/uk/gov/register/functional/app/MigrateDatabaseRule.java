package uk.gov.register.functional.app;

import com.codahale.metrics.MetricRegistry;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.flyway.FlywayFactory;
import org.flywaydb.core.Flyway;
import org.junit.rules.ExternalResource;

import javax.sql.DataSource;
import java.util.Collections;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

public class MigrateDatabaseRule extends ExternalResource {
    private final List<TestRegister> registers;

    public MigrateDatabaseRule(TestRegister... registers) {
        this.registers = newArrayList(registers);
    }

    @Override
    protected void before() {
        for (TestRegister register : registers) {
            FlywayFactory flywayFactory = getFlywayFactory(register.name());
            Flyway flyway = flywayFactory.build(getDataSource(register.getDatabaseConnectionString("MigrateDatabaseRule")));
            flyway.setSchemas(register.getSchema());
            flyway.migrate();
        }
    }

    private FlywayFactory getFlywayFactory(String registerId) {
        FlywayFactory flywayFactory = new FlywayFactory();
        flywayFactory.setLocations(Collections.singletonList("/sql"));
        flywayFactory.setPlaceholders(Collections.singletonMap("registerId", registerId));
        flywayFactory.setOutOfOrder(true);
        return flywayFactory;
    }

    private DataSource getDataSource(String databaseConnectionString) {
        DataSourceFactory dataSourceFactory = new DataSourceFactory();
        dataSourceFactory.setDriverClass("org.postgresql.Driver");
        dataSourceFactory.setUrl(databaseConnectionString);
        return dataSourceFactory.build(new MetricRegistry(), "ft_openregister_java_multi");
    }
}
