package uk.gov.register.core.datatype;

import com.fasterxml.jackson.databind.JsonNode;
import uk.gov.register.util.DateFormatChecker;

public class DatetimeDatatype implements Datatype {
    private final String datatypeName;

    public DatetimeDatatype(String datatypeName) {
        this.datatypeName = datatypeName;
    }

    @Override
    public boolean isValid(JsonNode value) {
        return DateFormatChecker.isDateFormatValid(value.textValue());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DatetimeDatatype that = (DatetimeDatatype) o;

        return datatypeName != null ? datatypeName.equals(that.datatypeName) : that.datatypeName == null;

    }

    @Override
    public int hashCode() {
        return datatypeName != null ? datatypeName.hashCode() : 0;
    }

    public String getName() {
        return datatypeName;
    }
}
