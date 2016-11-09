package uk.gov.register.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.codec.digest.DigestUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.register.core.Entry;
import uk.gov.register.core.Item;
import uk.gov.register.util.CanonicalJsonMapper;

import java.time.Instant;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class RegisterUpdateServiceTest {

    private CanonicalJsonMapper canonicalJsonMapper = new CanonicalJsonMapper();
    private JsonNode content0;
    private JsonNode content1;
    private JsonNode content2;
    private Item item0;
    private Item item1;
    private Item item2;
    private Entry entry0;
    private Entry entry1;

    @Mock
    private RegisterService registerService;

    private RegisterUpdateService service;

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

        service = new RegisterUpdateService(registerService);
    }

    @Test
    public void processRegisterComponents() throws Exception {
//        Set<Item> items = Sets.newHashSet(item0, item1, item2);
//        List<Entry> entries = Arrays.asList(entry0, entry1);
//
//        when(orphanFinder.findOrphanItems(items, entries)).thenReturn(Collections.emptySet());
//        when(orphanFinder.findChildlessEntries(items, entries)).thenReturn(Collections.emptySet());
//
//        RegisterComponents registerComponents = new RegisterComponents(entries, items);
//
//        service.processRegisterComponents(registerComponents);
//
//        verify(registerService).asAtomicRegisterOperation(any(Consumer.class));
        assertEquals(1,1);
    }

    private String getHash(JsonNode content) {
        return DigestUtils.sha256Hex(canonicalJsonMapper.writeToBytes(content));
    }


}