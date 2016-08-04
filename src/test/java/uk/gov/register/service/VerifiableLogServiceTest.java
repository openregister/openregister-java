package uk.gov.register.service;

import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.junit.Test;
import org.skife.jdbi.v2.ResultIterator;
import uk.gov.register.core.Entry;
import uk.gov.register.db.EntryQueryDAO;
import uk.gov.register.views.ConsistencyProof;
import uk.gov.register.views.EntryProof;
import uk.gov.register.views.RegisterProof;
import uk.gov.verifiablelog.store.memoization.MemoizationStore;

import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Optional;

import static com.google.common.collect.Lists.newArrayList;
import static org.apache.commons.codec.digest.DigestUtils.sha256Hex;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.register.service.FakeResultIterator.resultIterator;

public class VerifiableLogServiceTest {
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
        EntryQueryDAO entryDAO = createEntryDAOForEntries(entry1);
        VerifiableLogService vlService = createVerifiableLogService(entryDAO);

        RegisterProof registerProof = vlService.getRegisterProof();

        MatcherAssert.assertThat(registerProof.getProofIdentifier(), equalTo("merkle:sha-256"));
        MatcherAssert.assertThat(registerProof.getRootHash(), equalTo("d3d33f57b033d18ad11e14b28ef6f33487410c98548d1759c772370dfeb6db11"));
    }

    @Test
    public void shouldReturnValidRegisterProofForTwoEntries() throws Exception {
        EntryQueryDAO entryDAO = createEntryDAOForEntries(entry1, entry2);
        VerifiableLogService vlService =  createVerifiableLogService(entryDAO);

        RegisterProof registerProof = vlService.getRegisterProof();

        MatcherAssert.assertThat(registerProof.getProofIdentifier(), equalTo("merkle:sha-256"));
        MatcherAssert.assertThat(registerProof.getRootHash(), equalTo("e869291e3017a7b1dd6b16af0b556d75378bef59486f1a7f53586b3ca86aed09"));
    }

    @Test
    public void shouldReturnValidRegisterProofForThreeEntries() throws Exception {
        EntryQueryDAO entryDAO = createEntryDAOForEntries(entry1, entry2, entry3);
        VerifiableLogService vlService = createVerifiableLogService(entryDAO);

        RegisterProof registerProof = vlService.getRegisterProof();

        MatcherAssert.assertThat(registerProof.getProofIdentifier(), equalTo("merkle:sha-256"));
        MatcherAssert.assertThat(registerProof.getRootHash(), equalTo("6b85b168f7c5f0587fc22ff4ba6937e61b33f6e89b70eed53d78d895d35dc9c3"));
    }

    @Test
    public void shouldReturnValidEntryProof() throws NoSuchAlgorithmException {
        EntryQueryDAO entryDAO = createEntryDAOForEntries(entry1, entry2, entry3, entry4);
        VerifiableLogService vlService = createVerifiableLogService(entryDAO);

        EntryProof entryProof = vlService.getEntryProof(1, 4);

        MatcherAssert.assertThat(entryProof.getProofIdentifier(), equalTo("merkle:sha-256"));
        MatcherAssert.assertThat(entryProof.getEntryNumber(), equalTo("1"));
        MatcherAssert.assertThat(entryProof.getAuditPath(), hasSize(equalTo(2)));
        MatcherAssert.assertThat(entryProof.getAuditPath(), is(Arrays.asList(
                "d3d33f57b033d18ad11e14b28ef6f33487410c98548d1759c772370dfeb6db11", "0a95724bb3e5c1f28e2c2eb7c47c8ceee1200d7a262ba47a4217bb434e558fe5")));
    }

    @Test
    public void shouldReturnValidConsistencyProof() throws NoSuchAlgorithmException {
        EntryQueryDAO entryDAO = createEntryDAOForEntries(entry1, entry2, entry3, entry4);
        VerifiableLogService vlService = createVerifiableLogService(entryDAO);

        ConsistencyProof consistencyProof = vlService.getConsistencyProof(2, 4);

        MatcherAssert.assertThat(consistencyProof.getProofIdentifier(), equalTo("merkle:sha-256"));
        MatcherAssert.assertThat(consistencyProof.getConsistencyNodes(), hasSize(equalTo(1)));
        MatcherAssert.assertThat(consistencyProof.getConsistencyNodes(), is(Arrays.asList("e869291e3017a7b1dd6b16af0b556d75378bef59486f1a7f53586b3ca86aed09")));
    }

    private EntryQueryDAO createEntryDAOForEntries(Entry... entries) {
        EntryQueryDAO entryDAO = mock(EntryQueryDAO.class);

        when(entryDAO.getTotalEntries()).thenReturn(entries.length);
        when(entryDAO.entriesIteratorFrom(anyInt())).thenReturn(resultIterator(newArrayList(entries)));
        when(entryDAO.findByEntryNumber(anyInt())).thenAnswer(invocation -> {
            int arg = (int) invocation.getArguments()[0];
            return Optional.of(entries[arg]);
        });

        return entryDAO;
    }

    private VerifiableLogService createVerifiableLogService(EntryQueryDAO entryDAO) throws NoSuchAlgorithmException {
        return new VerifiableLogService(entryDAO, mock(MemoizationStore.class));
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

