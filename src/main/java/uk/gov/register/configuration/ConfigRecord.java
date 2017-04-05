package uk.gov.register.configuration;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * This class exists just to allow a Field object to be parsed from a json field record
 * where the model provides that the item is a single item in a list.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ConfigRecord<T> {

    @JsonProperty("item")
    private List<T> item;

    public void setItem(List<T> item) {
        this.item = item;
    }

    T getSingleItem(){
        if (item.size() != 0){
            return item.get(0);
        } else {
            throw new IllegalStateException("item list for field record did not contain a single item");
        }
    }
}
