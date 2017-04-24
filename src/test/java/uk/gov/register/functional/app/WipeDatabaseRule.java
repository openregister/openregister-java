package uk.gov.register.functional.app;

import org.junit.rules.ExternalResource;
import org.skife.jdbi.v2.DBI;
import uk.gov.register.db.SchemaRewriter;
import uk.gov.register.functional.db.TestEntryDAO;
import uk.gov.register.functional.db.TestIndexDAO;
import uk.gov.register.functional.db.TestItemCommandDAO;
import uk.gov.register.functional.db.TestRecordDAO;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

public class WipeDatabaseRule extends ExternalResource {
    private final List<String> registers;

    public WipeDatabaseRule(String... registers) {
        this.registers = newArrayList(registers);
    }

    private String postgresConnectionString() {
        return "jdbc:postgresql://localhost:5432/ft_openregister_java_multi?user=postgres&ApplicationName=WipeDatabaseRule";
    }

    @Override
    protected void before() {
        for (String register : registers) {
            SchemaRewriter.schema.set(register);
            DBI dbi = new DBI(postgresConnectionString());
            dbi.useHandle(handle -> {
                handle.attach(TestEntryDAO.class).wipeData();
                handle.attach(TestItemCommandDAO.class).wipeData();
                handle.attach(TestRecordDAO.class).wipeData();
                handle.attach(TestIndexDAO.class).wipeData();
            });
        }
    }
}

