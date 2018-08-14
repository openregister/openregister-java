package uk.gov.register.util;

public interface DateFormat {

    interface ISO_8601 {
        String LABEL = "ISO_8601";

        String YEAR = "yyyy";
        String YEAR_MONTH = "yyyy-MM";
        String YEAR_MONTH_DAY = "yyyy-MM-dd";
        String YEAR_MONTH_DAY_HOURS_MINUTES_SECONDS = "yyyy-MM-dd'T'hh:mm:ss";
        String YEAR_MONTH_DAY_HOURS_MINUTES_SECONDS_UTC = "yyyy-MM-dd'T'HH:mm:ss'Z'";
    }
}
