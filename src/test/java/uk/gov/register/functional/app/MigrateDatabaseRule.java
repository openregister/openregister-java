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
    private final List<String> registers;

    public MigrateDatabaseRule(String... registers) {
        this.registers = newArrayList(registers);
    }

    private String postgresConnectionString() {
        return "jdbc:postgresql://localhost:5432/ft_openregister_java_multi?user=postgres&ApplicationName=MigrateDatabaseRule";
    }

    @Override
    protected void before() {
        for (String register : registers) {
            FlywayFactory flywayFactory = getFlywayFactory(register);
            Flyway flyway = flywayFactory.build(getDataSource());
            flyway.setSchemas(register);
            flyway.migrate();
        }
    }

    private FlywayFactory getFlywayFactory(String registerName) {
        FlywayFactory flywayFactory = new FlywayFactory();
        flywayFactory.setLocations(Collections.singletonList("/sql"));
        flywayFactory.setPlaceholders(Collections.singletonMap("registerName", registerName));
        flywayFactory.setOutOfOrder(true);
        return flywayFactory;
    }

    private DataSource getDataSource() {
        DataSourceFactory dataSourceFactory = new DataSourceFactory();
        dataSourceFactory.setDriverClass("org.postgresql.Driver");
        dataSourceFactory.setUrl(postgresConnectionString());
        return dataSourceFactory.build(new MetricRegistry(), "ft_openregister_java_multi");
    }
}
