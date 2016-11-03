package uk.gov.register.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Sets;
import org.apache.commons.codec.digest.DigestUtils;
import org.junit.Before;
import org.junit.Test;
import uk.gov.register.core.Entry;
import uk.gov.register.core.Item;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class OrphanFinderTest {

    private CanonicalJsonMapper canonicalJsonMapper = new CanonicalJsonMapper();
    private JsonNode content0;
    private JsonNode content1;
    private JsonNode content2;
    private Item item0;
    private Item item1;
    private Item item2;
    private Entry entry0;
    private Entry entry1;

    @Before
    public void setUp() throws Exception {
        content0 = new ObjectMapper().readTree("{\"address\":\"9AQZJ3M\",\"name\":\"ST LAWRENCE CHURCH\"}");
        content1 = new ObjectMapper().readTree("{\"address\":\"9AQZJ3M\",\"name\":\"ST LAWRENCE CHURCH REMAINS OF\"}");
        content2 = new ObjectMapper().readTree("{\"address\":\"ZZZZZZZ\",\"name\":\"ST MARYS CHURCH\"}");

        item0 = new Item(content0);
        item1 = new Item(content1);
        item2 = new Item(content2);

        entry0 = new Entry(0, getHash(content0), Instant.now());
        entry1 = new Entry(1, getHash(content1), Instant.now());
    }

    @Test
    public void shouldFindOrphans() throws Exception {
        Set<Item> items = Sets.newHashSet(item0, item1, item2);
        List<Entry> entries = Arrays.asList(entry0, entry1);

        Set<Item> orphanItems = OrphanFinder.findOrphanItems(items, entries);
        assertThat( orphanItems.size(), is(1));

    }

    @Test
    public void shouldNotFindOrphans() throws Exception {
        Set<Item> items = Sets.newHashSet(item0, item1);
        List<Entry> entries = Arrays.asList(entry0, entry1);

        Set<Item> orphanItems = OrphanFinder.findOrphanItems(items, entries);
        assertThat( orphanItems.size(), is(0));

    }

    private String getHash(JsonNode content) {
        return DigestUtils.sha256Hex(canonicalJsonMapper.writeToBytes(content));
    }

}
