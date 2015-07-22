package uk.gov.register.presentation.view;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import uk.gov.register.presentation.Entry;
import uk.gov.register.presentation.representations.SingleResultJsonSerializer;

@JsonSerialize(using = SingleResultJsonSerializer.class)
public class SingleResultView extends AbstractView<Entry> {
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

    @Override
    public Entry get(){
        return entry;
    }
}
