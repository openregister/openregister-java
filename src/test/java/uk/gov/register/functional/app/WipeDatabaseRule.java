package uk.gov.register.functional.app;

import org.apache.log4j.MDC;
import org.junit.rules.ExternalResource;
import org.skife.jdbi.v2.DBI;
import uk.gov.register.functional.db.TestEntryDAO;
import uk.gov.register.functional.db.TestItemCommandDAO;
import uk.gov.register.functional.db.TestRecordDAO;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

public class WipeDatabaseRule extends ExternalResource {
    private final List<String> registers;

    public WipeDatabaseRule(String... registers) {
        this.registers = newArrayList(registers);
    }

    private String postgresConnectionString(String register) {
        return String.format("jdbc:postgresql://localhost:5432/ft_openregister_java_%s?user=postgres&ApplicationName=WipeDatabaseRule", register);
    }

    @Override
    protected void before() {
        for (String register : registers) {
            MDC.put("register", register);
            DBI dbi = new DBI(postgresConnectionString(register));
            dbi.useHandle(handle -> {
                handle.attach(TestEntryDAO.class).wipeData();
                handle.attach(TestItemCommandDAO.class).wipeData();
                handle.attach(TestRecordDAO.class).wipeData();
            });
        }
    }
}

