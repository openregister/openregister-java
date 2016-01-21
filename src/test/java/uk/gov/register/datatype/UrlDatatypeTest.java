package uk.gov.register.datatype;

import com.fasterxml.jackson.databind.node.TextNode;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class UrlDatatypeTest {
    UrlDatatype urlDatatype = new UrlDatatype("url");
    @Test
    public void isValid_true_whenAValidUrlStringValue() {
        assertTrue(urlDatatype.isValid(TextNode.valueOf("http://foo/ff")));
        assertTrue(urlDatatype.isValid(TextNode.valueOf("ftp://foo/ff")));
    }

    @Test
    public void isValid_true_whenValueIsEmptyString_becauseUrlValueCanBeAnOptionalInRegister(){
        assertTrue(urlDatatype.isValid(TextNode.valueOf("")));
    }

    @Test
    public void isValid_false_whenStringValueIsNotValidUrl() {
        assertFalse(urlDatatype.isValid(TextNode.valueOf("http//foo/ff")));
        assertFalse(urlDatatype.isValid(TextNode.valueOf("http;//www.rmplc.co.uk/eduweb/sites/barnwell/index.html")));
    }

}
