package uk.gov.register.util;

import org.junit.Before;
import org.junit.Test;
import uk.gov.register.exceptions.SerializationFormatValidationException;

import java.io.IOException;

import static org.junit.Assert.fail;

public class CanonicalJsonValidatorTest {
    private CanonicalJsonValidator canonicalJsonValidator;

    @Before
    public void setup() {
        canonicalJsonValidator = new CanonicalJsonValidator();
    }

    @Test(expected = SerializationFormatValidationException.class)
    public void validateItemIsCanonicalized_throwsValidationException_whenItemIsNotCanonicalized() {
        String jsonString = "{\"text\":\"some text\",\"register\":\"aregister\"}";

        canonicalJsonValidator.validateItemStringIsCanonicalized(jsonString);
    }

    @Test
    public void validateItemIsCanonicalized_throwsNoValidationException_whenItemIsCanonicalized() throws IOException {
        String jsonString = "{\"register\":\"aregister\",\"text\":\"some text\"}";

        try {
            canonicalJsonValidator.validateItemStringIsCanonicalized(jsonString);
        } catch (SerializationFormatValidationException e) {
            fail("Entry is canonicalized but exception was thrown");
        }
    }
}