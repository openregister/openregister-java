package uk.gov.register.presentation.view;

import com.fasterxml.jackson.annotation.JsonValue;
import uk.gov.register.presentation.EntryView;
import uk.gov.register.presentation.resource.RequestContext;
import uk.gov.register.thymeleaf.ThymeleafView;

public class SingleEntryView extends ThymeleafView {
    private final EntryView entryView;
    private final String versionHistoryLink;

    SingleEntryView(RequestContext requestContext, EntryView entryView) {
        this(requestContext, entryView, "entry.html");
    }

    SingleEntryView(RequestContext requestContext, EntryView entryView, String templateName) {
        super(requestContext, templateName);
        this.entryView = entryView;
        versionHistoryLink = constructVersionHistoryLink(entryView, requestContext.getRegisterPrimaryKey());
    }

    private String constructVersionHistoryLink(EntryView entryView, String primaryKey) {
        return String.format("/%s/%s/history",
                primaryKey,
                entryView.getContent().get(primaryKey).value());
    }

    @JsonValue
    public EntryView getEntry() {
        return entryView;
    }

    @SuppressWarnings("unused")
    public String getVersionHistoryLink(){
        return versionHistoryLink;
    }
}

