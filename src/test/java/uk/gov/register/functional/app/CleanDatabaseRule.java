package uk.gov.register.functional.app;

import org.junit.rules.ExternalResource;
import org.skife.jdbi.v2.util.StringColumnMapper;

import java.util.List;

import static uk.gov.register.functional.db.TestDBSupport.handle;

public class CleanDatabaseRule extends ExternalResource {
    @Override
    protected void before() {
        List<String> tables = handle.createQuery("select tablename from pg_tables where schemaname = 'public'").map(StringColumnMapper.INSTANCE).list();
        for (String table : tables) {
            handle.update("drop table " + table + " CASCADE");
        }
    }
}

