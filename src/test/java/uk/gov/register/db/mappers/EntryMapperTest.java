package uk.gov.register.db.mappers;

import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.skife.jdbi.v2.DBI;
import uk.gov.register.core.Entry;
import uk.gov.register.core.HashingAlgorithm;
import uk.gov.register.db.EntryQueryDAO;
import uk.gov.register.functional.app.MigrateDatabaseRule;
import uk.gov.register.functional.app.WipeDatabaseRule;
import uk.gov.register.util.HashValue;

import java.time.Instant;
import java.util.Collection;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static uk.gov.register.functional.app.TestRegister.address;

public class EntryMapperTest {
    private final DBI dbi = new DBI(address.getDatabaseConnectionString("EntryMapperTest"));

    @ClassRule
    public static MigrateDatabaseRule migrateDatabaseRule = new MigrateDatabaseRule(address);
    @Rule
    public WipeDatabaseRule wipeDatabaseRule = new WipeDatabaseRule(address);

    @Test
    public void map_returnsSameDateAndTimeInUTC() throws Exception {
        String expected = "2016-07-15T10:00:00Z";
        Instant expectedInstant = Instant.parse(expected);

        Collection<Entry> allEntriesNoPagination = dbi.withHandle(h -> {
            h.execute("insert into address.entry(entry_number, timestamp, sha256hex) values(5, :timestamp, 'abcdef')", expectedInstant.getEpochSecond());
            h.execute("insert into address.entry_item(entry_number, sha256hex) values(5, 'abcdef')");
            // this method implicitly invokes EntryMapper
            return h.attach(EntryQueryDAO.class).getAllEntriesNoPagination();
        });

        assertThat(allEntriesNoPagination.size(), equalTo(1));
        assertThat(allEntriesNoPagination.iterator().next().getTimestamp(), equalTo(expectedInstant));
    }

    @Test
    public void map_returnsSingleItemHashForEntry() {
        Collection<Entry> allEntriesNoPagination = dbi.withHandle(h -> {
            h.execute("insert into address.entry(entry_number, timestamp, sha256hex) values(5, :timestamp, 'abcdef')", Instant.now().getEpochSecond());
            h.execute("insert into address.entry_item(entry_number, sha256hex) values(5, 'ghijkl')");
            return h.attach(EntryQueryDAO.class).getAllEntriesNoPagination();
        });

        Entry entry = allEntriesNoPagination.iterator().next();

        assertThat(allEntriesNoPagination.size(), equalTo(1));
        assertThat(entry.getItemHashes(), contains(new HashValue(HashingAlgorithm.SHA256, "ghijkl")));
    }

    @Test
    public void map_returnsMultipleItemHashesForEntry() {
        Collection<Entry> allEntriesNoPagination = dbi.withHandle(h -> {
            h.execute("insert into address.entry(entry_number, timestamp, sha256hex) values(5, :timestamp, 'abcdef')", Instant.now().getEpochSecond());
            h.execute("insert into address.entry_item(entry_number, sha256hex) values(5, 'abcdef')");
            h.execute("insert into address.entry_item(entry_number, sha256hex) values(5, 'ghijkl')");
            return h.attach(EntryQueryDAO.class).getAllEntriesNoPagination();
        });

        Entry entry = allEntriesNoPagination.iterator().next();

        assertThat(allEntriesNoPagination.size(), equalTo(1));
        assertThat(entry.getItemHashes(), containsInAnyOrder(new HashValue(HashingAlgorithm.SHA256, "abcdef"), new HashValue(HashingAlgorithm.SHA256, "ghijkl")));
    }

    @Test
    public void map_returnsNoItemHashesForEntry() {
        Collection<Entry> allEntriesNoPagination = dbi.withHandle(h -> {
            h.execute("insert into address.entry(entry_number, timestamp, sha256hex) values(5, :timestamp, 'abcdef')", Instant.now().getEpochSecond());
            return h.attach(EntryQueryDAO.class).getAllEntriesNoPagination();
        });

        Entry entry = allEntriesNoPagination.iterator().next();

        assertThat(allEntriesNoPagination.size(), equalTo(1));
        assertThat(entry.getItemHashes().size(), equalTo(0));
    }
}
