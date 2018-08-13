package uk.gov.register.core.datatype;

import com.fasterxml.jackson.databind.node.TextNode;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PeriodDatatypeTest {
    PeriodDatatype periodDatatype = new PeriodDatatype("period");

    @Test
    public void isValid_true_whenValueIsDuration() {
        assertTrue(periodDatatype.isValid(TextNode.valueOf("P5Y1M")));
        assertTrue(periodDatatype.isValid(TextNode.valueOf("PT8M10S")));
        assertTrue(periodDatatype.isValid(TextNode.valueOf("P10Y10M10DT10H10M10S")));
        assertTrue(periodDatatype.isValid(TextNode.valueOf("P5Y1MT8M10S")));
    }

    @Test
    public void isValid_false_whenValueIsNotDuration() {
        assertFalse(periodDatatype.isValid(TextNode.valueOf("P")));
        assertFalse(periodDatatype.isValid(TextNode.valueOf("PT")));
        assertFalse(periodDatatype.isValid(TextNode.valueOf("P1YT")));
        assertFalse(periodDatatype.isValid(TextNode.valueOf("1Y1M")));
        assertFalse(periodDatatype.isValid(TextNode.valueOf("T1M1S")));
        assertFalse(periodDatatype.isValid(TextNode.valueOf("PxY")));
    }

    @Test
    public void isValid_true_whenValueIsPeriod() {
        assertTrue(periodDatatype.isValid(TextNode.valueOf("2007-03-01T13:00:00Z/2007-03-01T15:30:00Z")));
        assertTrue(periodDatatype.isValid(TextNode.valueOf("2007-03-01/2007-03-02")));
        assertTrue(periodDatatype.isValid(TextNode.valueOf("2007-03-01T13:00:00Z/P1Y2M10DT2H30M")));
        assertTrue(periodDatatype.isValid(TextNode.valueOf("P1Y2M10DT2H30M/2007-03-01T13:00:00Z")));
    }

    @Test
    public void isValid_false_whenValueIsNotPeriod() {
        assertFalse(periodDatatype.isValid(TextNode.valueOf("2007-03-01T15:30:00Z/2007-03-01T13:00:00Z")));
        assertFalse(periodDatatype.isValid(TextNode.valueOf("2007-03-01T15:30:00Z|2007-03-01T13:00:00Z")));
        assertFalse(periodDatatype.isValid(TextNode.valueOf("P1Y2M10DT2H30M/P1Y2M")));

    }
}
