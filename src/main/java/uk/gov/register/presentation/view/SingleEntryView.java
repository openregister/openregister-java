package uk.gov.register.presentation.view;

import com.fasterxml.jackson.annotation.JsonValue;
import uk.gov.register.presentation.EntryView;
import uk.gov.register.presentation.resource.RequestContext;
import uk.gov.register.thymeleaf.ThymeleafView;

public class SingleEntryView extends ThymeleafView {
    private final EntryView entryView;
    private String versionHistoryLink;

    SingleEntryView(RequestContext requestContext, EntryView entryView) {
        super(requestContext, "entry.html");
        this.entryView = entryView;
    }

    SingleEntryView(RequestContext requestContext, EntryView entryView, String templateName) {
        super(requestContext, templateName);
        this.entryView = entryView;
    }

    @JsonValue
    public EntryView getEntry() {
        return entryView;
    }

    @SuppressWarnings("unused")
    public String getVersionHistoryLink(){
        return versionHistoryLink;
    }

    public void setVersionHistoryLink(String versionHistoryLink){
        this.versionHistoryLink =versionHistoryLink;
    }
}

