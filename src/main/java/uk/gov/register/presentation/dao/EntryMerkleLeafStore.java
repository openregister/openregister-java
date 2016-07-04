package uk.gov.register.presentation.dao;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import uk.gov.verifiablelog.store.MerkleLeafStore;

public class EntryMerkleLeafStore implements MerkleLeafStore {

    private final EntryDAO entryDAO;
    private final EntryIteratorDAO entryIteratorDAO;

    public EntryMerkleLeafStore(EntryDAO entryDAO) {
        this.entryDAO = entryDAO;
        this.entryIteratorDAO = new EntryIteratorDAO(entryDAO);
    }

    @Override
    public byte[] getLeafValue(int i) {
        return bytesFromEntry(entryIteratorDAO.findByEntryNumber(i + 1));
    }

    @Override
    public int totalLeaves() {
        return entryDAO.getTotalEntries();
    }

    private byte[] bytesFromEntry(Entry entry) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);
            mapper.getFactory().configure(JsonGenerator.Feature.ESCAPE_NON_ASCII, false);

            String value = mapper.writeValueAsString(entry);
            return value.getBytes();
        } catch (JsonProcessingException e) {
            // FIXME swallow for now and return null byte
            return new byte[]{0x00};
        }
    }
}
