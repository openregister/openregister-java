package uk.gov.register.presentation.view;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.dropwizard.views.View;
import uk.gov.register.presentation.Entry;
import uk.gov.register.presentation.representations.SingleResultJsonSerializer;

@JsonSerialize(using = SingleResultJsonSerializer.class)
public class SingleResultView extends View {
    private final String hash;
    private final JsonNode content;
    private final Entry entry;

    public SingleResultView(String templateName, Entry entry) {
        super(templateName);
        this.hash = entry.getHash();
        this.content = entry.getContent();
        this.entry = entry;
    }

    public String getHash() {
        return hash;
    }

    public JsonNode getEntry() {
        return content;
    }

    public Entry getObject(){
        return entry;
    }
}
