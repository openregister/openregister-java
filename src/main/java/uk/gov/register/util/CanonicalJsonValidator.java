package uk.gov.register.util;

import uk.gov.register.exceptions.SerializationFormatValidationException;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class CanonicalJsonValidator {
    private final JsonMapper jsonMapper;
    private final CanonicalJsonMapper canonicalJsonMapper;

    public CanonicalJsonValidator() {
        this.jsonMapper = new JsonMapper();
        this.canonicalJsonMapper = new CanonicalJsonMapper();
    }

    public void validateItemStringIsCanonicalized(String jsonItem) {
        byte[] jsonItemBytes = jsonItem.getBytes(StandardCharsets.UTF_8);
        byte[] nonCanonicalizedBytes = jsonMapper.writeToBytes(jsonMapper.readFromBytes(jsonItemBytes));
        byte[] canonicalizedBytes = canonicalJsonMapper.writeToBytes(canonicalJsonMapper.readFromBytes(jsonItemBytes));

        if (!Arrays.equals(nonCanonicalizedBytes, canonicalizedBytes)) {
            throw new SerializationFormatValidationException(jsonItem);
        }
    }
}