package uk.gov.register.presentation;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static java.time.format.DateTimeFormatter.ISO_INSTANT;

public class ISODateFormatter {
    public static String format(Instant instant) {
        // http://openregister.github.io/specification/#timestamp-datatype only allows seconds, not fractions thereof
        return ISO_INSTANT.format(instant.truncatedTo(ChronoUnit.SECONDS));
    }
}
