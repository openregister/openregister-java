package uk.gov.register.presentation.view;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import uk.gov.register.presentation.Entry;

public class SingleResultView extends AbstractView<JsonNode> {
    private final String hash;
    private final JsonNode content;
    private final Entry entry;

    public SingleResultView(String templateName, Entry entry) {
        super(templateName);
        this.hash = entry.getHash();
        this.content = entry.getContent();
        this.entry = entry;
    }

    @Override
    public JsonNode get() {
        return content;
    }

    @JsonProperty
    public String getHash() {
        return hash;
    }

    @JsonProperty
    public JsonNode getEntry() {
        return content;
    }

    @JsonIgnore
    public Entry getEntryObject(){
        return entry;
    }
}
