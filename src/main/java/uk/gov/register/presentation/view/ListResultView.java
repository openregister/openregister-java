package uk.gov.register.presentation.view;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import uk.gov.register.presentation.RecordView;
import uk.gov.register.presentation.representations.ListResultJsonSerializer;
import uk.gov.register.presentation.resource.RequestContext;
import uk.gov.register.thymeleaf.ThymeleafView;

import java.util.List;

@JsonSerialize(using = ListResultJsonSerializer.class)
public class ListResultView extends ThymeleafView {
    private final List<RecordView> records;

    ListResultView(RequestContext requestContext, List<RecordView> records) {
        super(requestContext, "entries.html");
        this.records = records;
    }

    public List<RecordView> getRecords() {
        return records;
    }
}
