package uk.gov.register.functional.db;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import uk.gov.register.db.EntryIteratorDAO;
import uk.gov.register.db.EntryQueryDAO;
import uk.gov.register.functional.app.RegisterRule;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class EntryIteratorDAOFunctionalTest {

    private EntryIteratorDAO entryIteratorDAO;

    @ClassRule
    public static final RegisterRule register = new RegisterRule();
    private static final TestEntryDAO testEntryDAO = register.handleFor("register").attach(TestEntryDAO.class);

    @Before
    public void publishTestMessages() {
        register.wipe();

        EntryQueryDAO entryQueryDAO = mock(EntryQueryDAO.class);
        when(entryQueryDAO.entriesIteratorFrom(anyInt())).thenAnswer(invocation ->
                testEntryDAO.entriesIteratorFrom(invocation.getArgument(0)));

        entryIteratorDAO = new EntryIteratorDAO(entryQueryDAO);
    }

    @Test
    public void testCanIterateInOrder() {
        register.loadRsf("register", "add-item\t{\"fields\":[\"field1\"],\"register\":\"value1\",\"text\":\"The Entry 1\"}\n" +
                "add-item\t{\"fields\":[\"field1\",\"field2\"],\"register\":\"value2\",\"text\":\"The Entry 2\"}\n" +
                "append-entry\t2016-03-01T01:02:03Z\tsha-256:877d8bd1ab71dc6e48f64b4ca83c6d7bf645a1eb56b34d50fa8a833e1101eb18\tvalue1\n" +
                "append-entry\t2016-03-02T02:03:04Z\tsha-256:63e5a0453b088e39265ca9f20fd03e2b206422e32989649adaca84426b531cd7\tvalue2\n");

        assertThat(entryIteratorDAO.findByEntryNumber(1).getEntryNumber(), equalTo(1));
        assertThat(entryIteratorDAO.findByEntryNumber(2).getEntryNumber(), equalTo(2));
    }

    @Test
    public void testCanIterateNotInOrder() {
        register.loadRsf("register", "add-item\t{\"fields\":[\"field1\"],\"register\":\"value1\",\"text\":\"The Entry 1\"}\n" +
                "add-item\t{\"fields\":[\"field1\",\"field2\"],\"register\":\"value2\",\"text\":\"The Entry 2\"}\n" +
                "append-entry\t2016-03-01T01:02:03Z\tsha-256:877d8bd1ab71dc6e48f64b4ca83c6d7bf645a1eb56b34d50fa8a833e1101eb18\tvalue1\n" +
                "append-entry\t2016-03-02T02:03:04Z\tsha-256:63e5a0453b088e39265ca9f20fd03e2b206422e32989649adaca84426b531cd7\tvalue2\n");

        assertThat(entryIteratorDAO.findByEntryNumber(2).getEntryNumber(), equalTo(2));
        assertThat(entryIteratorDAO.findByEntryNumber(1).getEntryNumber(), equalTo(1));
    }

    @Test
    public void testCanStillIterateAfterIteratorEndsThenNewEntriesAdded() {
        register.loadRsf("register", "add-item\t{\"fields\":[\"field1\"],\"register\":\"value1\",\"text\":\"The Entry 1\"}\n" +
                "append-entry\t2016-03-01T01:02:03Z\tsha-256:877d8bd1ab71dc6e48f64b4ca83c6d7bf645a1eb56b34d50fa8a833e1101eb18\tvalue1\n");

        assertThat(entryIteratorDAO.findByEntryNumber(1).getEntryNumber(), equalTo(1));

        register.loadRsf("register", "add-item\t{\"fields\":[\"field1\",\"field2\"],\"register\":\"value2\",\"text\":\"The Entry 2\"}\n" +
                "append-entry\t2016-03-02T02:03:04Z\tsha-256:63e5a0453b088e39265ca9f20fd03e2b206422e32989649adaca84426b531cd7\tvalue2\n");

        assertThat(entryIteratorDAO.findByEntryNumber(2).getEntryNumber(), equalTo(2));
    }

    @Test
    public void testCanStillIterateAfterIteratorDoesNotThenNewEntriesAddedd() {
        register.loadRsf("register", "add-item\t{\"fields\":[\"field1\"],\"register\":\"value1\",\"text\":\"The Entry 1\"}\n" +
                "add-item\t{\"fields\":[\"field1\",\"field2\"],\"register\":\"value2\",\"text\":\"The Entry 2\"}\n" +
                "append-entry\t2016-03-01T01:02:03Z\tsha-256:877d8bd1ab71dc6e48f64b4ca83c6d7bf645a1eb56b34d50fa8a833e1101eb18\tvalue1\n" +
                "append-entry\t2016-03-02T02:03:04Z\tsha-256:63e5a0453b088e39265ca9f20fd03e2b206422e32989649adaca84426b531cd7\tvalue2\n");

        assertThat(entryIteratorDAO.findByEntryNumber(1).getEntryNumber(), equalTo(1));

        register.loadRsf("register", "add-item\t{\"fields\":[\"field1\",\"field2\",\"field3\"],\"register\":\"value3\",\"text\":\"The Entry 3\"}\n" +
                "append-entry\t2016-03-01T01:02:03Z\tsha-256:8d5c2ed1e59f8871dff6e2132171008f12f43aa37e70ab158d598bc6b6db848f\tvalue1\n");

        assertThat(entryIteratorDAO.findByEntryNumber(2).getEntryNumber(), equalTo(2));
        assertThat(entryIteratorDAO.findByEntryNumber(3).getEntryNumber(), equalTo(3));
    }
}
