package uk.gov.register.util;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;

import java.text.ParseException;

import static uk.gov.register.util.DateFormat.ISO_8601.*;

public class DateFormatChecker {

    public static boolean isDateFormatValid(final String dateToValidate) {
        if (StringUtils.isEmpty(dateToValidate)) {
            return true;
        }

        try {
            DateUtils.parseDate(dateToValidate, YEAR, YEAR_MONTH, YEAR_MONTH_DAY, YEAR_MONTH_DAY_HOURS_MINUTES_SECONDS, YEAR_MONTH_DAY_HOURS_MINUTES_SECONDS_UTC);

            return true;
        } catch (ParseException e) {
            return false;
        }
    }
}
