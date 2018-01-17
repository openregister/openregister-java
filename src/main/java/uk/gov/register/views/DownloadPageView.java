package uk.gov.register.views;

import uk.gov.register.core.RegisterReadOnly;
import uk.gov.register.core.RegisterResolver;
import uk.gov.register.thymeleaf.ThymeleafView;
import uk.gov.register.resources.RequestContext;

public class DownloadPageView extends ThymeleafView {

    private final Boolean downloadEnabled;

    public DownloadPageView(RequestContext requestContext, RegisterReadOnly register, Boolean enableDownloadResource, RegisterResolver registerResolver) {
        super(requestContext, "download.html", registerResolver, register);
        this.downloadEnabled = enableDownloadResource;
    }

    public Boolean getDownloadEnabled() {
        return downloadEnabled;
    }
}
