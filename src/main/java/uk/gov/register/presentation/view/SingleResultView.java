package uk.gov.register.presentation.view;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.dropwizard.views.View;
import uk.gov.register.presentation.Record;
import uk.gov.register.presentation.representations.SingleResultJsonSerializer;

@JsonSerialize(using = SingleResultJsonSerializer.class)
public class SingleResultView extends View {
    private final String hash;
    private final JsonNode content;
    private final Record record;

    public SingleResultView(String templateName, Record record) {
        super(templateName);
        this.hash = record.getHash();
        this.content = record.getContent();
        this.record = record;
    }

    public String getHash() {
        return hash;
    }

    public JsonNode getRecord() {
        return content;
    }

    public Record getObject(){
        return record;
    }
}
