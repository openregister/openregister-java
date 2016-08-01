package uk.gov.register.core.datatype;

import com.fasterxml.jackson.databind.node.TextNode;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class UrlDatatypeTest {
    UrlDatatype urlDatatype = new UrlDatatype("url");
    @Test
    public void isValid_true_onlyWhenAValidURlStringValue() {
        assertTrue(urlDatatype.isValid(TextNode.valueOf("http://foo/ff")));
        assertTrue(urlDatatype.isValid(TextNode.valueOf("ftp://foo/ff")));
    }

    @Test
    public void isValid_false_whenStringValueIsNotValidUrl() {
        assertFalse(urlDatatype.isValid(TextNode.valueOf("http//foo/ff")));
    }
}
