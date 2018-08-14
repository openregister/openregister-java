package uk.gov.register.util;

import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class DateFormatCheckerTest {

    @Test
    public void validateFormat_shouldValidateSuccessfully_whenInputDateTimeIsOfFormatYYYY() throws IOException {
        assertTrue("Format should be valid", DateFormatChecker.isDateTimeFormatValid("2012"));
    }

    @Test
    public void validateFormat_shouldValidateSuccessfully_whenInputDateTimeIsOfFormatYYYYMM() throws IOException {
        assertTrue("Format should be valid", DateFormatChecker.isDateTimeFormatValid("2012-01"));
    }

    @Test
    public void validateFormat_shouldValidateSuccessfully_whenInputDateTimeIsOfFormatYYYYMMDD() throws IOException {
        assertTrue("Format should be valid", DateFormatChecker.isDateTimeFormatValid("2012-01-01"));
    }

    @Test
    public void validateFormat_shouldValidateSuccessfully_whenInputDateTimeIsOfFormatYYYYMMDDThhmmss() throws IOException {
        assertTrue("Format should be valid", DateFormatChecker.isDateTimeFormatValid("2012-01-01T01:01:01"));
    }

    @Test
    public void validateFormat_shouldValidateSuccessfully_whenInputDateTimeIsOfFormatYYYYMMDDThhmmssZ() throws IOException {
        assertTrue("Format should be valid", DateFormatChecker.isDateTimeFormatValid("2012-01-01T01:01:01Z"));
    }

    @Test
    public void validateFormat_shouldValidateSuccessfully_whenInputDateTimeIsOfFormatYYYYMMDDThhmmssZWithTwoDigitHour() throws IOException {
        assertTrue("Format should be valid", DateFormatChecker.isDateTimeFormatValid("2012-01-01T11:01:01Z"));
    }

    @Test
    public void validateFormat_throwsValidationException_whenInputDateTimeIsNotValid() throws IOException {
        assertFalse("Format should not be valid", DateFormatChecker.isDateTimeFormatValid("2012/01/01"));
    }

    @Test
    public void validateFormat_throwsValidationException_whenInputDateTimeIsEmpty() throws IOException {
        assertFalse("Format should not be valid", DateFormatChecker.isDateTimeFormatValid(""));
    }

    @Test
    public void validateFormat_throwsValidationException_whenInputDateTimeIsFiveDigitNumber() throws IOException {
        assertFalse("Format should not be valid", DateFormatChecker.isDateTimeFormatValid("41365"));
    }

    @Test
    public void validateFormat_throwsValidationException_whenInputDateTimeUsesNonUtcTimeZone() throws IOException {
        assertFalse("Format should not be valid", DateFormatChecker.isDateTimeFormatValid("2008-09-15T15:53:00+05:00"));
    }

    @Test
    public void validateFormat_throwsValidationException_whenInputDateTimeIsUnPaddedMonth() throws IOException {
        assertFalse("Format should not be valid", DateFormatChecker.isDateTimeFormatValid("2018-1"));
    }

    @Test
    public void validateDateOrder_shouldValidateSuccessfully_whenInputDatesAreOrdered() throws IOException {
        assertTrue("Dates should be ordered", DateFormatChecker.isDateTimeFormatsOrdered("2012-01-01T01:01:01Z", "2012-01-01T01:01:02Z"));
    }

    @Test
    public void validateDateOrder_shouldNotValidate_whenInputDatesAreNotOrdered() throws IOException {
        assertFalse("Dates should not be ordered", DateFormatChecker.isDateTimeFormatsOrdered("2012-01-01T01:01:02Z", "2012-01-01T01:01:01Z"));
    }
}
