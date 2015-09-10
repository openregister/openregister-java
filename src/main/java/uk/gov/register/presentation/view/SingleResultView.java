package uk.gov.register.presentation.view;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import uk.gov.register.presentation.RecordView;
import uk.gov.register.presentation.representations.SingleResultJsonSerializer;
import uk.gov.register.presentation.resource.RequestContext;
import uk.gov.register.thymeleaf.ThymeleafView;

@JsonSerialize(using = SingleResultJsonSerializer.class)
public class SingleResultView extends ThymeleafView {
    private final RecordView recordView;

    SingleResultView(RequestContext requestContext, RecordView recordView) {
        super(requestContext, "entry.html");
        this.recordView = recordView;
    }

    public RecordView getRecord() {
        return recordView;
    }
}

