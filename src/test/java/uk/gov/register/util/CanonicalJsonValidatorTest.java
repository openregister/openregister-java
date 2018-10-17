package uk.gov.register.util;

import org.junit.Before;
import org.junit.Test;
import uk.gov.register.exceptions.BlobNotCanonicalException;

import java.io.IOException;

import static org.junit.Assert.fail;

public class CanonicalJsonValidatorTest {
    private CanonicalJsonValidator canonicalJsonValidator;

    @Before
    public void setup() {
        canonicalJsonValidator = new CanonicalJsonValidator();
    }

    @Test(expected = BlobNotCanonicalException.class)
    public void validateItemIsCanonicalized_throwsValidationException_whenItemIsNotCanonicalizedByFieldOrder() {
        String jsonString = "{\"text\":\"some text\",\"register\":\"aregister\"}";

        canonicalJsonValidator.validateBlobStringIsCanonicalized(jsonString);
    }

    @Test(expected = BlobNotCanonicalException.class)
    public void validateItemIsCanonicalized_throwsValidationException_whenItemIsNotCanonicalizedByWhitespace() {
        String jsonString = "{ \"register\":\"aregister\", \"text\":\"some text\"  }";

        canonicalJsonValidator.validateBlobStringIsCanonicalized(jsonString);
    }

    @Test
    public void validateItemIsCanonicalized_throwsNoValidationException_whenItemIsCanonicalized() throws IOException {
        String jsonString = "{\"register\":\"aregister\",\"text\":\"some text\"}";

        try {
            canonicalJsonValidator.validateBlobStringIsCanonicalized(jsonString);
        } catch (BlobNotCanonicalException e) {
            fail("BaseEntry is canonicalized but exception was thrown");
        }
    }
}
