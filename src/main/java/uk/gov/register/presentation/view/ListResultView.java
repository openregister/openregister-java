package uk.gov.register.presentation.view;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import uk.gov.register.presentation.Record;
import uk.gov.register.presentation.representations.ListResultJsonSerializer;
import uk.gov.register.presentation.resource.RequestContext;
import uk.gov.register.thymeleaf.ThymeleafView;

import java.util.List;

@JsonSerialize(using = ListResultJsonSerializer.class)
public class ListResultView extends ThymeleafView {
    private final List<Record> records;

    ListResultView(RequestContext requestContext, List<Record> records) {
        super(requestContext, "entries.html");
        this.records = records;
    }

    public List<Record> getRecords() {
        return records;
    }
}
