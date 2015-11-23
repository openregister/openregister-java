package uk.gov.register.presentation.view;

import com.fasterxml.jackson.annotation.JsonValue;
import uk.gov.register.presentation.EntryView;
import uk.gov.register.presentation.config.PublicBodiesConfiguration;
import uk.gov.register.presentation.resource.RequestContext;
import uk.gov.register.thymeleaf.ThymeleafView;

public class SingleEntryView extends ThymeleafView {
    private final EntryView entryView;
    private final String versionHistoryLink;

    SingleEntryView(RequestContext requestContext, EntryView entryView, PublicBodiesConfiguration publicBodiesConfiguration) {
        this(requestContext, entryView, publicBodiesConfiguration, "entry.html");
    }

    SingleEntryView(RequestContext requestContext, EntryView entryView, PublicBodiesConfiguration publicBodiesConfiguration, String templateName) {
        super(requestContext, publicBodiesConfiguration, templateName);
        this.entryView = entryView;
        versionHistoryLink = constructVersionHistoryLink(entryView, requestContext.getRegisterPrimaryKey());
    }

    private String constructVersionHistoryLink(EntryView entryView, String primaryKey) {
        return String.format("/%s/%s/history",
                primaryKey,
                entryView.getContent().get(primaryKey).getValue());
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

