package uk.gov.register.util;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;

import java.text.ParseException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;
import java.util.Date;

import static uk.gov.register.util.DateFormat.ISO_8601.*;

public class DateFormatChecker {

    public static boolean isDateTimeFormatValid(String dateToValidate) {
        return !StringUtils.isEmpty(dateToValidate) && isValidFormat(dateToValidate);
    }

    public static boolean isDateTimeFormatsOrdered(String startString, String endString) {
        try {
            Date start = parseDateTime(startString);
            Date end = parseDateTime(endString);

            return start.before(end);
        } catch (ParseException ex) {
            return false;
        }
    }

    private static Date parseDateTime(String dateTimeString) throws ParseException {
        return DateUtils.parseDateStrictly(dateTimeString, YEAR, YEAR_MONTH, YEAR_MONTH_DAY, YEAR_MONTH_DAY_HOURS_MINUTES_SECONDS, YEAR_MONTH_DAY_HOURS_MINUTES_SECONDS_UTC);
    }

    private static boolean isValidFormat(String dateTimeString) {

        DateTimeFormatter[] formatters = {
                new DateTimeFormatterBuilder()
                        .appendPattern(YEAR)
                        .parseDefaulting(ChronoField.MONTH_OF_YEAR, 1)
                        .parseDefaulting(ChronoField.DAY_OF_MONTH, 1)
                        .parseStrict()
                        .toFormatter(),
                new DateTimeFormatterBuilder()
                        .appendPattern(YEAR_MONTH)
                        .parseDefaulting(ChronoField.DAY_OF_MONTH, 1)
                        .parseStrict()
                        .toFormatter(),
                new DateTimeFormatterBuilder()
                        .appendPattern(YEAR_MONTH_DAY)
                        .parseStrict()
                        .toFormatter(),
                new DateTimeFormatterBuilder()
                        .appendPattern(YEAR_MONTH_DAY_HOURS_MINUTES_SECONDS)
                        .parseStrict()
                        .toFormatter(),
                new DateTimeFormatterBuilder()
                        .appendPattern(YEAR_MONTH_DAY_HOURS_MINUTES_SECONDS_UTC)
                        .toFormatter()
        };

        for (final DateTimeFormatter formatter : formatters) {
            try {
                LocalDate.parse(dateTimeString, formatter);
                return true;
            } catch (DateTimeParseException ignored) {
            }
        }
        return false;
    }
}
