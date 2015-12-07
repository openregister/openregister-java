package uk.gov.register.datatype;

import com.fasterxml.jackson.databind.JsonNode;

import java.net.MalformedURLException;
import java.net.URL;

public class UrlDatatype extends StringDatatype {
    @Override
    public boolean isValid(JsonNode value) {
        return super.isValid(value) && validUrl(value);
    }

    private boolean validUrl(JsonNode value) {
        String url = value.textValue();
        try{
            new URL(url);
            return true;
        }catch(MalformedURLException e){
            return false;
        }
    }
}
