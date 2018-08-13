package uk.gov.register.core.datatype;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.lang3.StringUtils;

import java.time.format.DateTimeParseException;
import java.util.regex.Pattern;

import static uk.gov.register.util.DateFormatChecker.isDateTimeFormatValid;
import static uk.gov.register.util.DateFormatChecker.isDateTimeFormatsOrdered;

public class PeriodDatatype extends AbstractDatatype {

    public PeriodDatatype(String datatypeName) {
        super(datatypeName);
    }

    @Override
    public boolean isValid(JsonNode value) {
        if (!value.isTextual() && StringUtils.isBlank(value.textValue())) {
            return false;
        }

        return isValidPeriod(value.textValue());
    }

    private boolean isValidPeriod(String periodToValidate) {
        if (StringUtils.isEmpty(periodToValidate)) {
            return false;
        }

        String[] periodParts = periodToValidate.split("/");

        if (periodParts.length == 1) {
            return isValidDuration(periodParts[0]);
        } else if (periodParts.length == 2) {
            String firstPart = periodParts[0];
            String secondPart = periodParts[1];

            if (isDateTimeFormatValid(firstPart)) {
                if (isDateTimeFormatValid(secondPart)) {
                    return isDateTimeFormatsOrdered(firstPart, secondPart);
                } else {
                    return isValidDuration(secondPart);
                }
            } else if (isValidDuration(firstPart)) {
                return isDateTimeFormatValid(secondPart);
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    private static boolean isValidDuration(String duration) {
        try {
            if (duration == "P" || duration.endsWith("T")) {
                return false;
            }
            return Pattern.matches("P(\\d++Y)?(\\d++M)?(\\d++D)?(T(\\d++H)?(\\d++M)?(\\d++S)?)?", duration);
        } catch (DateTimeParseException ex) {
            return false;
        }
    }
}
