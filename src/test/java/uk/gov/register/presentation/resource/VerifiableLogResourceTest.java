package uk.gov.register.presentation.resource;

import org.junit.Test;
import org.skife.jdbi.v2.ResultIterator;
import uk.gov.register.presentation.AuditProof;
import uk.gov.register.presentation.ConsistencyProof;
import uk.gov.register.presentation.RegisterProof;
import uk.gov.register.presentation.dao.Entry;
import uk.gov.register.presentation.dao.EntryQueryDAO;
import uk.gov.verifiablelog.store.memoization.DoNothing;

import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Optional;

import static com.google.common.collect.Lists.newArrayList;
import static org.apache.commons.codec.digest.DigestUtils.sha256Hex;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.register.presentation.resource.FakeResultIterator.resultIterator;

public class VerifiableLogResourceTest {
    private static final String item1 = "{\"address\":\"6789\",\"name\":\"presley\"}";
    private static final String item2 = "{\"address\":\"6789\",\"name\":\"presley2\"}";
    private static final String item3 = "{\"address\":\"6790\",\"name\":\"rose cottage\"}";
    private static final String item4 = "{\"address\":\"6790\",\"name\":\"rose cottage2\"}";

    private static final Entry entry1 = new Entry("1", sha256Hex(item1), Instant.parse("2016-07-01T11:21:30.00Z"));
    private static final Entry entry2 = new Entry("2", sha256Hex(item2), Instant.parse("2016-07-01T11:21:35.00Z"));
    private static final Entry entry3 = new Entry("3", sha256Hex(item3), Instant.parse("2016-07-01T11:22:10.00Z"));
    private static final Entry entry4 = new Entry("4", sha256Hex(item4), Instant.parse("2016-07-01T11:24:10.00Z"));

    @Test
    public void shouldReturnValidRegisterProofForOneEntry() throws Exception {
        EntryQueryDAO entryDAO = entryDAOForEntries(entry1);
        VerifiableLogResource verifiableLogResource = new VerifiableLogResource(entryDAO, new DoNothing());

        RegisterProof registerProof = verifiableLogResource.registerProof();

        assertThat(registerProof.getProofIdentifier(), equalTo("merkle:sha-256"));
        assertThat(registerProof.getRootHash(), equalTo("d3d33f57b033d18ad11e14b28ef6f33487410c98548d1759c772370dfeb6db11"));
    }

    @Test
    public void shouldReturnValidRegisterProofForTwoEntries() throws Exception {
        EntryQueryDAO entryDAO = entryDAOForEntries(entry1, entry2);
        VerifiableLogResource verifiableLogResource = new VerifiableLogResource(entryDAO, new DoNothing());

        RegisterProof registerProof = verifiableLogResource.registerProof();

        assertThat(registerProof.getProofIdentifier(), equalTo("merkle:sha-256"));
        assertThat(registerProof.getRootHash(), equalTo("e869291e3017a7b1dd6b16af0b556d75378bef59486f1a7f53586b3ca86aed09"));
    }

    @Test
    public void shouldReturnValidRegisterProofForThreeEntries() throws Exception {
        EntryQueryDAO entryDAO = entryDAOForEntries(entry1, entry2, entry3);
        VerifiableLogResource verifiableLogResource = new VerifiableLogResource(entryDAO, new DoNothing());

        RegisterProof registerProof = verifiableLogResource.registerProof();

        assertThat(registerProof.getProofIdentifier(), equalTo("merkle:sha-256"));
        assertThat(registerProof.getRootHash(), equalTo("6b85b168f7c5f0587fc22ff4ba6937e61b33f6e89b70eed53d78d895d35dc9c3"));
    }

    @Test
    public void shouldReturnValidAuditProof() throws NoSuchAlgorithmException {
        EntryQueryDAO entryDAO = entryDAOForEntries(entry1, entry2, entry3, entry4);
        VerifiableLogResource verifiableLogResource = new VerifiableLogResource(entryDAO, new DoNothing());

        AuditProof auditProof = verifiableLogResource.auditProof(4, 1);

        assertThat(auditProof.getProofIdentifier(), equalTo("merkle:sha-256"));
        assertThat(auditProof.getEntryNumber(), equalTo("1"));
        assertThat(auditProof.getAuditPath(), hasSize(equalTo(2)));
        assertThat(auditProof.getAuditPath(), is(Arrays.asList(
                "d3d33f57b033d18ad11e14b28ef6f33487410c98548d1759c772370dfeb6db11", "0a95724bb3e5c1f28e2c2eb7c47c8ceee1200d7a262ba47a4217bb434e558fe5")));
    }

    @Test
    public void shouldReturnValidConsistencyProof() throws NoSuchAlgorithmException {
        EntryQueryDAO entryDAO = entryDAOForEntries(entry1, entry2, entry3, entry4);
        VerifiableLogResource verifiableLogResource = new VerifiableLogResource(entryDAO, new DoNothing());

        ConsistencyProof consistencyProof = verifiableLogResource.consistencyProof(2, 4);

        assertThat(consistencyProof.getProofIdentifier(), equalTo("merkle:sha-256"));
        assertThat(consistencyProof.getConsistencyNodes(), hasSize(equalTo(1)));
        assertThat(consistencyProof.getConsistencyNodes(), is(Arrays.asList("e869291e3017a7b1dd6b16af0b556d75378bef59486f1a7f53586b3ca86aed09")));
    }

    private EntryQueryDAO entryDAOForEntries(Entry... entries) {
        EntryQueryDAO entryDAO = mock(EntryQueryDAO.class);

        when(entryDAO.getTotalEntries()).thenReturn(entries.length);
        when(entryDAO.entriesIteratorFrom(anyInt())).thenReturn(resultIterator(newArrayList(entries)));
        when(entryDAO.findByEntryNumber(anyInt())).thenAnswer(invocation -> {
            int arg = (int) invocation.getArguments()[0];
            return Optional.of(entries[arg]);
        });

        return entryDAO;
    }
}

class FakeResultIterator<T> implements ResultIterator<T> {
    private final Iterator<T> underlying;

    private FakeResultIterator(Iterator<T> underlying) {
        this.underlying = underlying;
    }

    public static <T> FakeResultIterator<T> resultIterator(Iterable<T> underlying) {
        return new FakeResultIterator<>(underlying.iterator());
    }

    @Override
    public void close() {
        //ignore
    }

    @Override
    public boolean hasNext() {
        return underlying.hasNext();
    }

    @Override
    public T next() {
        return underlying.next();
    }
}
