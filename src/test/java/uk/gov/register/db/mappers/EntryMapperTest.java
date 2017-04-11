package uk.gov.register.db.mappers;

import org.junit.Rule;
import org.junit.Test;
import org.skife.jdbi.v2.DBI;
import uk.gov.register.core.Entry;
import uk.gov.register.core.HashingAlgorithm;
import uk.gov.register.db.EntryQueryDAO;
import uk.gov.register.functional.app.WipeDatabaseRule;
import uk.gov.register.util.HashValue;

import java.time.Instant;
import java.util.Collection;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class EntryMapperTest {
    @Rule
    public WipeDatabaseRule wipeDatabaseRule = new WipeDatabaseRule("address");

    @Test
    public void map_returnsSameDateAndTimeInUTC() throws Exception {
        String expected = "2016-07-15T10:00:00Z";
        Instant expectedInstant = Instant.parse(expected);

        DBI dbi = new DBI("jdbc:postgresql://localhost:5432/ft_openregister_java_address?user=postgres&ApplicationName=EntryMapperTest");

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
        DBI dbi = new DBI("jdbc:postgresql://localhost:5432/ft_openregister_java_address?user=postgres&ApplicationName=EntryMapperTest");

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
        DBI dbi = new DBI("jdbc:postgresql://localhost:5432/ft_openregister_java_address?user=postgres&ApplicationName=EntryMapperTest");

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
        DBI dbi = new DBI("jdbc:postgresql://localhost:5432/ft_openregister_java_address?user=postgres&ApplicationName=EntryMapperTest");

        Collection<Entry> allEntriesNoPagination = dbi.withHandle(h -> {
            h.execute("insert into address.entry(entry_number, timestamp, sha256hex) values(5, :timestamp, 'abcdef')", Instant.now().getEpochSecond());
            return h.attach(EntryQueryDAO.class).getAllEntriesNoPagination();
        });

        Entry entry = allEntriesNoPagination.iterator().next();

        assertThat(allEntriesNoPagination.size(), equalTo(1));
        assertThat(entry.getItemHashes().size(), equalTo(0));
    }
}
