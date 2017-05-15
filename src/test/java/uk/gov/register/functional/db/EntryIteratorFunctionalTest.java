package uk.gov.register.functional.db;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import uk.gov.register.db.EntryIterator;
import uk.gov.register.db.EntryQueryDAO;
import uk.gov.register.functional.app.RegisterRule;
import uk.gov.register.functional.app.TestRegister;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class EntryIteratorFunctionalTest {

    private EntryQueryDAO entryQueryDAO;

    @ClassRule
    public static final RegisterRule register = new RegisterRule();
    private static final TestEntryDAO testEntryDAO = register.handleFor(TestRegister.register).attach(TestEntryDAO.class);

    @Before
    public void publishTestMessages() {
        register.wipe();

        entryQueryDAO = mock(EntryQueryDAO.class);
        when(entryQueryDAO.entriesIteratorFrom(anyInt())).thenAnswer(invocation ->
                testEntryDAO.entriesIteratorFrom(invocation.getArgument(0)));
    }

    @Test
    public void testCanIterateInOrder() {
        register.loadRsf(TestRegister.register, "add-item\t{\"fields\":[\"field1\"],\"register\":\"value1\",\"text\":\"The Entry 1\"}\n" +
                "add-item\t{\"fields\":[\"field1\",\"field2\"],\"register\":\"value2\",\"text\":\"The Entry 2\"}\n" +
                "append-entry\tvalue1\t2016-03-01T01:02:03Z\tsha-256:877d8bd1ab71dc6e48f64b4ca83c6d7bf645a1eb56b34d50fa8a833e1101eb18\n" +
                "append-entry\tvalue2\t2016-03-02T02:03:04Z\tsha-256:63e5a0453b088e39265ca9f20fd03e2b206422e32989649adaca84426b531cd7\n");

        EntryIterator.withEntryIterator(entryQueryDAO, entryIteratorDAO -> {
            assertThat(entryIteratorDAO.findByEntryNumber(1).getEntryNumber(), equalTo(1));
            assertThat(entryIteratorDAO.findByEntryNumber(2).getEntryNumber(), equalTo(2));
            return null;
        });
    }

    @Test
    public void testCanIterateNotInOrder() {
        register.loadRsf(TestRegister.register, "add-item\t{\"fields\":[\"field1\"],\"register\":\"value1\",\"text\":\"The Entry 1\"}\n" +
                "add-item\t{\"fields\":[\"field1\",\"field2\"],\"register\":\"value2\",\"text\":\"The Entry 2\"}\n" +
                "append-entry\tvalue1\t2016-03-01T01:02:03Z\tsha-256:877d8bd1ab71dc6e48f64b4ca83c6d7bf645a1eb56b34d50fa8a833e1101eb18\n" +
                "append-entry\tvalue2\t2016-03-02T02:03:04Z\tsha-256:63e5a0453b088e39265ca9f20fd03e2b206422e32989649adaca84426b531cd7\n");

        EntryIterator.withEntryIterator(entryQueryDAO, entryIteratorDAO -> {
            assertThat(entryIteratorDAO.findByEntryNumber(2).getEntryNumber(), equalTo(2));
            assertThat(entryIteratorDAO.findByEntryNumber(1).getEntryNumber(), equalTo(1));
            return null;
        });
    }

    @Test
    public void testCanStillIterateAfterIteratorEndsThenNewEntriesAdded() {
        EntryIterator.withEntryIterator(entryQueryDAO, entryIteratorDAO -> {
            register.loadRsf(TestRegister.register, "add-item\t{\"fields\":[\"field1\"],\"register\":\"value1\",\"text\":\"The Entry 1\"}\n" +
                    "append-entry\tvalue1\t2016-03-01T01:02:03Z\tsha-256:877d8bd1ab71dc6e48f64b4ca83c6d7bf645a1eb56b34d50fa8a833e1101eb18\n");

            assertThat(entryIteratorDAO.findByEntryNumber(1).getEntryNumber(), equalTo(1));

            register.loadRsf(TestRegister.register, "add-item\t{\"fields\":[\"field1\",\"field2\"],\"register\":\"value2\",\"text\":\"The Entry 2\"}\n" +
                    "append-entry\tvalue2\t2016-03-02T02:03:04Z\tsha-256:63e5a0453b088e39265ca9f20fd03e2b206422e32989649adaca84426b531cd7\n");

            assertThat(entryIteratorDAO.findByEntryNumber(2).getEntryNumber(), equalTo(2));
            return null;
        });
    }

    @Test
    public void testCanStillIterateAfterIteratorDoesNotThenNewEntriesAddedd() {
        EntryIterator.withEntryIterator(entryQueryDAO, entryIteratorDAO -> {
            register.loadRsf(TestRegister.register, "add-item\t{\"fields\":[\"field1\"],\"register\":\"value1\",\"text\":\"The Entry 1\"}\n" +
                    "add-item\t{\"fields\":[\"field1\",\"field2\"],\"register\":\"value2\",\"text\":\"The Entry 2\"}\n" +
                    "append-entry\tvalue1\t2016-03-01T01:02:03Z\tsha-256:877d8bd1ab71dc6e48f64b4ca83c6d7bf645a1eb56b34d50fa8a833e1101eb18\n" +
                    "append-entry\tvalue2\t2016-03-02T02:03:04Z\tsha-256:63e5a0453b088e39265ca9f20fd03e2b206422e32989649adaca84426b531cd7\n");

            assertThat(entryIteratorDAO.findByEntryNumber(1).getEntryNumber(), equalTo(1));

            register.loadRsf(TestRegister.register, "add-item\t{\"fields\":[\"field1\",\"field2\",\"field3\"],\"register\":\"value3\",\"text\":\"The Entry 3\"}\n" +
                    "append-entry\tvalue1\t2016-03-01T01:02:03Z\tsha-256:8d5c2ed1e59f8871dff6e2132171008f12f43aa37e70ab158d598bc6b6db848f\n");

            assertThat(entryIteratorDAO.findByEntryNumber(2).getEntryNumber(), equalTo(2));
            assertThat(entryIteratorDAO.findByEntryNumber(3).getEntryNumber(), equalTo(3));
            return null;
        });
    }
}
