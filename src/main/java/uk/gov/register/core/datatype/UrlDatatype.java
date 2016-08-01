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
