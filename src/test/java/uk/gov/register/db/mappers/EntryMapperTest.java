package uk.gov.register.db.mappers;

import org.junit.Rule;
import org.junit.Test;
import org.skife.jdbi.v2.DBI;
import uk.gov.register.core.Entry;
import uk.gov.register.db.EntryQueryDAO;
import uk.gov.register.functional.app.WipeDatabaseRule;

import java.time.Instant;
import java.util.Collection;

import static org.hamcrest.Matchers.equalTo;
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
            h.execute("insert into entry(entry_number, timestamp, sha256hex) values(5, :timestamp, 'abcdef')", expectedInstant.getEpochSecond());
            h.execute("insert into entry_item(entry_number, sha256hex) values(5, 'abcdef')");
            // this method implicitly invokes EntryMapper
            return h.attach(EntryQueryDAO.class).getAllEntriesNoPagination();
        });

        assertThat(allEntriesNoPagination.size(), equalTo(1));
        assertThat(allEntriesNoPagination.iterator().next().getTimestamp(), equalTo(expectedInstant));
    }
}
