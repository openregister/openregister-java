package uk.gov.register.db;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import uk.gov.register.core.Entry;
import uk.gov.register.views.EntryView;
import uk.gov.verifiablelog.store.MerkleLeafStore;

public class V1EntryMerkleLeafStore implements MerkleLeafStore {
    private final EntryIterator entryIterator;


    public V1EntryMerkleLeafStore(EntryIterator entryIterator) {
        this.entryIterator = entryIterator;
    }

    @Override
    public byte[] getLeafValue(int i) {
        return bytesFromEntry(entryIterator.findByEntryNumber(i + 1));
    }

    @Override
    public int totalLeaves() {
        return entryIterator.getTotalEntries();
    }

    private byte[] bytesFromEntry(Entry entry) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);
            mapper.getFactory().configure(JsonGenerator.Feature.ESCAPE_NON_ASCII, false);
            EntryView entryView = new EntryView(entry);
            String value = mapper.writeValueAsString(entryView);
            return value.getBytes();
        } catch (JsonProcessingException e) {
            // FIXME swallow for now and return null byte
            return new byte[]{0x00};
        }
    }
}
