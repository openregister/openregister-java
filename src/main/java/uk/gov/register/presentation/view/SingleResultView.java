package uk.gov.register.presentation.view;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import uk.gov.register.presentation.Record;
import uk.gov.register.presentation.representations.SingleResultJsonSerializer;
import uk.gov.register.presentation.resource.RequestContext;
import uk.gov.register.thymeleaf.ThymeleafView;

@JsonSerialize(using = SingleResultJsonSerializer.class)
public class SingleResultView extends ThymeleafView {
    private final Record record;

    SingleResultView(RequestContext requestContext, Record record) {
        super(requestContext, "entry.html");
        this.record = record;
    }

    public Record getRecord() {
        return record;
    }
}

