package uk.gov.register.core.datatype;

import com.fasterxml.jackson.databind.node.IntNode;
import com.fasterxml.jackson.databind.node.TextNode;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class IntegerDatatypeTest {
    IntegerDatatype integerDatatype = new IntegerDatatype("integer");

    @Test
    public void isValid_true_whenValueIsAnIntegerValue() {
        assertTrue(integerDatatype.isValid(TextNode.valueOf("5")));
        assertTrue(integerDatatype.isValid(TextNode.valueOf("0")));
        assertTrue(integerDatatype.isValid(TextNode.valueOf("1500")));
        assertTrue(integerDatatype.isValid(TextNode.valueOf("-1500")));
    }

    @Test
    public void isValid_false_whenValueIsNotAnIntegerValue() {
        assertFalse(integerDatatype.isValid(TextNode.valueOf("5a")));
        assertFalse(integerDatatype.isValid(TextNode.valueOf("-0")));
        assertFalse(integerDatatype.isValid(TextNode.valueOf("007")));
        assertFalse(integerDatatype.isValid(TextNode.valueOf("-007")));
    }

    @Test
    public void isValid_false_whenValueIsNotAStringNode() {
        assertFalse(integerDatatype.isValid(IntNode.valueOf(5)));
    }
}
