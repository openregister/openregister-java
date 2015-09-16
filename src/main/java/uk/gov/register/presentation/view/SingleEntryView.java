package uk.gov.register.presentation.view;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import uk.gov.register.presentation.EntryView;
import uk.gov.register.presentation.representations.SingleEntryJsonSerializer;
import uk.gov.register.presentation.resource.RequestContext;
import uk.gov.register.thymeleaf.ThymeleafView;

@JsonSerialize(using = SingleEntryJsonSerializer.class)
public class SingleEntryView extends ThymeleafView {
    private final EntryView entryView;

    SingleEntryView(RequestContext requestContext, EntryView entryView) {
        super(requestContext, "entry.html");
        this.entryView = entryView;
    }

    SingleEntryView(RequestContext requestContext, EntryView entryView, String templateName) {
        super(requestContext, templateName);
        this.entryView = entryView;
    }

    public EntryView getEntry() {
        return entryView;
    }
}

