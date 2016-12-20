package uk.gov.register.core.datatype;

import com.fasterxml.jackson.databind.JsonNode;

import java.net.MalformedURLException;
import java.net.URL;

public class UrlDatatype implements Datatype {
    private final String datatypeName;

    public UrlDatatype(String datatypeName) {
        this.datatypeName = datatypeName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UrlDatatype that = (UrlDatatype) o;

        return datatypeName != null ? datatypeName.equals(that.datatypeName) : that.datatypeName == null;

    }

    @Override
    public int hashCode() {
        return datatypeName != null ? datatypeName.hashCode() : 0;
    }

    @Override
    public boolean isValid(JsonNode value) {
        String url = value.textValue();
        try {
            new URL(url);
            return true;
        } catch (MalformedURLException e) {
            return false;
        }
    }

    public String getName() {
        return datatypeName;
    }
}
