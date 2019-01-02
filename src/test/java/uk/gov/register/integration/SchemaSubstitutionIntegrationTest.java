package uk.gov.register.integration;

import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.skife.jdbi.v2.DBI;
import uk.gov.register.core.Item;
import uk.gov.register.db.ItemQueryDAO;
import uk.gov.register.functional.app.MigrateDatabaseRule;
import uk.gov.register.functional.app.WipeDatabaseRule;

import java.util.Collection;

import static org.hamcrest.Matchers.is;
import static uk.gov.register.functional.app.TestRegister.local_authority_eng;

public class SchemaSubstitutionIntegrationTest {
    private DBI dbi;

    @ClassRule
    public static MigrateDatabaseRule migrateDatabaseRule = new MigrateDatabaseRule(local_authority_eng);

    @Rule
    public WipeDatabaseRule wipeDatabaseRule = new WipeDatabaseRule(local_authority_eng);

    @Before
    public void setup() {
        dbi = new DBI(local_authority_eng.getDatabaseConnectionString("PGRegisterTxnFT"));
    }

    @Test
    public void shouldSuccessfullyQueryWhenSchemaContainsHyphens(){
        Collection<Item> items = dbi.withHandle(handle -> handle.attach(ItemQueryDAO.class).getAllItemsNoPagination("local-authority-eng"));
        Assert.assertThat(items.size(), is(0));
    }

}
