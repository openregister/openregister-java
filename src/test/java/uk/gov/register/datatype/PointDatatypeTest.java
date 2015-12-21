package uk.gov.register.datatype;

import com.fasterxml.jackson.databind.node.TextNode;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PointDatatypeTest {
    PointDatatype pointDatatype = new PointDatatype("point");

    @Test
    public void isValid_true_onlyWhenValueIsAValidPoint() {
        assertTrue(pointDatatype.isValid(TextNode.valueOf("[123.123, -123.432]")));
        assertTrue(pointDatatype.isValid(TextNode.valueOf("[0.123, 123.432]")));
        assertTrue(pointDatatype.isValid(TextNode.valueOf("[ 0.123, 12.432 ]")));
    }

    @Test
    public void isValid_false_whenValueIsNotAValidPoint() {
        assertFalse(pointDatatype.isValid(TextNode.valueOf("123.123, 12.11")));
        assertFalse(pointDatatype.isValid(TextNode.valueOf("[ 123.123, 12.11")));
        assertFalse(pointDatatype.isValid(TextNode.valueOf("[ 123.123, as ]")));
        assertFalse(pointDatatype.isValid(TextNode.valueOf("[ 123.123, 12.111, 13.11 ]")));
    }
}
