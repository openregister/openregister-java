package uk.gov.register.presentation.view;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import uk.gov.register.presentation.EntryView;
import uk.gov.register.presentation.representations.ListResultJsonSerializer;
import uk.gov.register.presentation.resource.RequestContext;
import uk.gov.register.thymeleaf.ThymeleafView;

import java.util.List;

@JsonSerialize(using = ListResultJsonSerializer.class)
public class EntryListView extends ThymeleafView {
    private final List<EntryView> entries;

    EntryListView(RequestContext requestContext, List<EntryView> entries, String templateName) {
        super(requestContext, templateName);
        this.entries = entries;
    }

    public List<EntryView> getEntries() {
        return entries;
    }
}
