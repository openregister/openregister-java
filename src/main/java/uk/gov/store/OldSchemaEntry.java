package uk.gov.store;

import com.fasterxml.jackson.databind.JsonNode;
import uk.gov.mint.CanonicalJsonMapper;

@Deprecated
public class OldSchemaEntry {
    private static CanonicalJsonMapper canonicalJsonMapper = new CanonicalJsonMapper();
    public int id;
    public JsonNode entry;

    public OldSchemaEntry(int id, byte[] content) {
        this.id = id;
        this.entry = canonicalJsonMapper.readFromBytes(content);
    }
}
