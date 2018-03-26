package uk.gov.register.core.datatype;

import com.fasterxml.jackson.databind.JsonNode;
import uk.gov.register.util.DateFormatChecker;

public class DatetimeDatatype extends AbstractDatatype {

    public DatetimeDatatype(String datatypeName) {
        super(datatypeName);
    }

    @Override
    public boolean isValid(JsonNode value) {
        return DateFormatChecker.isDateTimeFormatValid(value.textValue());
    }
}
