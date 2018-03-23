package uk.gov.register.util;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;

import java.text.ParseException;
import java.util.Date;

import static uk.gov.register.util.DateFormat.ISO_8601.*;

public class DateFormatChecker {

    public static boolean isDateTimeFormatValid(String dateToValidate) {
        if (StringUtils.isEmpty(dateToValidate)) {
            return false;
        }

        try {
            parseDateTime(dateToValidate);
            return true;
        } catch (ParseException ex) {
            return false;
        }
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
        return DateUtils.parseDate(dateTimeString, YEAR, YEAR_MONTH, YEAR_MONTH_DAY, YEAR_MONTH_DAY_HOURS_MINUTES_SECONDS, YEAR_MONTH_DAY_HOURS_MINUTES_SECONDS_UTC);
    }
}
