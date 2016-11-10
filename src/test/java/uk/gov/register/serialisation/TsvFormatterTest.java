package uk.gov.register.serialisation;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.tomakehurst.wiremock.common.Json;
import org.junit.Test;

import uk.gov.register.core.Entry;
import uk.gov.register.core.Item;
import uk.gov.register.views.RegisterProof;

import java.time.Instant;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;


public class TsvFormatterTest {
//    private final TsvFormatter sutFormatter = new TsvFormatter();
//
//    @Test
//    public void shouldFormatEntryAsTsvLine(){
//        Instant entryTimestamp = Instant.parse("2016-07-15T10:00:00Z");
//        Entry entry = new Entry(1, "item-hash", entryTimestamp);
//
//        String actualLine = sutFormatter.format(entry);
//
//        assertThat(actualLine, equalTo("append-entry\t2016-07-15T10:00:00Z\tsha-256:item-hash\n"));
//    }
//
//    @Test
//    public void shouldFormatItemAsTsvLine(){
//        JsonNode itemContent = Json.read("{\"b\": \"2\",\"a\": \"1\" }", JsonNode.class);
//        Item item = new Item(itemContent);
//
//        String actualLine = sutFormatter.format(item);
//
//        assertThat(actualLine, equalTo("add-item\t{\"a\":\"1\",\"b\":\"2\"}\n"));
//    }
//
//    @Test
//    public void shouldFormatProofAsTsvLine(){
//        RegisterProof registerProof = new RegisterProof("root-hash");
//
//        String actualLine = sutFormatter.format(registerProof);
//
//        assertThat(actualLine, equalTo("assert-root-hash\troot-hash\n"));
//    }

}


