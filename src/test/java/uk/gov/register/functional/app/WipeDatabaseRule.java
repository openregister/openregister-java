package uk.gov.register.functional.app;

import org.junit.rules.ExternalResource;
import org.skife.jdbi.v2.DBI;
import uk.gov.register.functional.db.TestEntryDAO;
import uk.gov.register.functional.db.TestIndexDAO;
import uk.gov.register.functional.db.TestBlobCommandDAO;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

public class WipeDatabaseRule extends ExternalResource {
    private final List<TestRegister> registers;

    public WipeDatabaseRule(TestRegister... registers) {
        this.registers = newArrayList(registers);
    }

    @Override
    protected void before() {
        for (TestRegister register : registers) {
            DBI dbi = new DBI(register.getDatabaseConnectionString("WipeDatabaseRule"));
            dbi.useHandle(handle -> {
                handle.attach(TestEntryDAO.class).wipeData(register.getSchema());
                handle.attach(TestBlobCommandDAO.class).wipeData(register.getSchema());
                handle.attach(TestIndexDAO.class).wipeData(register.getSchema());
            });
        }
    }
}

