package uk.gov.register.configuration;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import uk.gov.register.core.RegisterMetadata;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class exists just to allow a RegisterMetadata object to be parsed from a json register record
 * where the model provides that the item is a single item in a list.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class RegisterConfigRecord {

    @JsonProperty("item")
    private List<RegisterMetadata> item;

    private Map<String, JsonNode> otherProperties = new HashMap<>();

    @SuppressWarnings("unused, used by jackson")
    @JsonAnySetter
    public void setOtherProperty(String name, JsonNode value) {
        otherProperties.put(name, value);
    }

    public void setItem(List<RegisterMetadata> item) {
        this.item = item;
    }

    RegisterMetadata getSingleItem(){
        if (item.size() != 0){
            RegisterMetadata registerMetadata = item.get(0);
            registerMetadata.getOtherProperties().putAll(otherProperties);
            return registerMetadata;
        } else {
            throw new IllegalStateException("item list for field record did not contain a single item");
        }
    }
}
