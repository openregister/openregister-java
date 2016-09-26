package uk.gov.register.db.mappers;

import org.junit.Test;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;
import uk.gov.register.core.Entry;
import uk.gov.register.db.EntryQueryDAO;
import uk.gov.register.db.SchemaCreator;

import java.time.Instant;
import java.util.Collection;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

public class EntryMapperTest {
    @Test
    public void map_returnsSameDateAndTimeInUTC() throws Exception {
        String expected = "2016-07-15T10:00:00Z";
        Instant expectedInstant = Instant.parse(expected);

        DBI dbi = new DBI("jdbc:postgresql://localhost:5432/test_openregister_java?user=postgres");
        Handle h = dbi.open();
        EntryQueryDAO entryQueryDAO = dbi.onDemand(EntryQueryDAO.class);
        SchemaCreator schemaCreator = dbi.onDemand(SchemaCreator.class);

        h.execute("drop table if exists entry; drop table if exists current_entry_number");
        schemaCreator.ensureSchema();
        h.execute("insert into entry(entry_number, sha256hex, timestamp) values(5, 'abcdef', :timestamp)",expectedInstant.getEpochSecond());

        // this method implicitly invokes EntryMapper
        Collection<Entry> allEntriesNoPagination = entryQueryDAO.getAllEntriesNoPagination();

        assertThat(allEntriesNoPagination.size(), equalTo(1));
        assertThat(allEntriesNoPagination.iterator().next().getTimestamp(), equalTo(expectedInstant));
    }
}
