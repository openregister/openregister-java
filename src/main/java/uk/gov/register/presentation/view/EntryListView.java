package uk.gov.register.presentation.view;

import com.fasterxml.jackson.annotation.JsonValue;
import uk.gov.register.presentation.EntryView;
import uk.gov.register.presentation.resource.RequestContext;
import uk.gov.register.thymeleaf.ThymeleafView;

import java.util.List;

public class EntryListView extends ThymeleafView {
    private final List<EntryView> entries;

    EntryListView(RequestContext requestContext, List<EntryView> entries, String templateName) {
        super(requestContext, templateName);
        this.entries = entries;
    }

    @JsonValue
    public List<EntryView> getEntries() {
        return entries;
    }
}
