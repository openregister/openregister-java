package uk.gov.register.presentation.view;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import uk.gov.register.presentation.EntryView;
import uk.gov.register.presentation.representations.SingleResultJsonSerializer;
import uk.gov.register.presentation.resource.RequestContext;
import uk.gov.register.thymeleaf.ThymeleafView;

@JsonSerialize(using = SingleResultJsonSerializer.class)
public class SingleResultView extends ThymeleafView {
    private final EntryView entryView;

    SingleResultView(RequestContext requestContext, EntryView entryView) {
        super(requestContext, "entry.html");
        this.entryView = entryView;
    }

    public EntryView getEntry() {
        return entryView;
    }
}

