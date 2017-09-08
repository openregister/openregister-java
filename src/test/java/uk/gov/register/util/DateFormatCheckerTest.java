package uk.gov.register.util;

import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class DateFormatCheckerTest {

    @Test
    public void validateFormat_shouldValidateSuccessfully_whenInputDateTimeIsOfFormatYYYY() throws IOException {
        assertTrue("Format should be valid", DateFormatChecker.isDateFormatValid("2012"));
    }

    @Test
    public void validateFormat_shouldValidateSuccessfully_whenInputDateTimeIsOfFormatYYYYDDMM() throws IOException {
        assertTrue("Format should be valid", DateFormatChecker.isDateFormatValid("2012-01"));
    }

    @Test
    public void validateFormat_shouldValidateSuccessfully_whenInputDateTimeIsOfFormatYYYYMMDD() throws IOException {
        assertTrue("Format should be valid", DateFormatChecker.isDateFormatValid("2012-01-01"));
    }

    @Test
    public void validateFormat_shouldValidateSuccessfully_whenInputDateTimeIsOfFormatYYYYMMDDThhmmss() throws IOException {
        assertTrue("Format should be valid", DateFormatChecker.isDateFormatValid("2012-01-01T01:01:01"));
    }

    @Test
    public void validateFormat_shouldValidateSuccessfully_whenInputDateTimeIsOfFormatYYYYMMDDThhmmssZ() throws IOException {
        assertTrue("Format should be valid", DateFormatChecker.isDateFormatValid("2012-01-01T01:01:01Z"));
    }

    @Test
    public void validateFormat_throwsValidationException_whenInputDateTimeIsNotValid() throws IOException {
        assertFalse("Format should not be valid", DateFormatChecker.isDateFormatValid("2012/01/01"));
    }

    @Test
    public void validateFormat_shouldValidateSuccessfully_whenInputDateTimeIsEmpty() throws IOException {
        assertTrue("Format should be valid", DateFormatChecker.isDateFormatValid(""));
    }
}
