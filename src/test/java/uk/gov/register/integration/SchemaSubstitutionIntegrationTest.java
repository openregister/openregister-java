package uk.gov.register.integration;

import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.skife.jdbi.v2.DBI;
import uk.gov.register.core.Blob;
import uk.gov.register.db.BlobQueryDAO;
import uk.gov.register.functional.app.MigrateDatabaseRule;

import java.util.Collection;

import static org.hamcrest.Matchers.is;
import static uk.gov.register.functional.app.TestRegister.local_authority_eng;

public class SchemaSubstitutionIntegrationTest {
    private DBI dbi;

    @ClassRule
    public static MigrateDatabaseRule migrateDatabaseRule = new MigrateDatabaseRule(local_authority_eng);
    @Before
    public void setup() {
        dbi = new DBI(local_authority_eng.getDatabaseConnectionString("PGRegisterTxnFT"));
    }

    @Test
    public void shouldSuccessfullyQueryWhenSchemaContainsHyphens(){
        Collection<Blob> blobs = dbi.withHandle(handle -> handle.attach(BlobQueryDAO.class).getAllBlobsNoPagination("local-authority-eng"));
        Assert.assertThat(blobs.size(), is(0));
    }

}
