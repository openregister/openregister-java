package uk.gov.register.views;

import uk.gov.register.configuration.RegisterTrackingConfiguration;
import uk.gov.register.core.RegisterData;
import uk.gov.register.core.RegisterResolver;
import uk.gov.register.thymeleaf.ThymeleafView;
import uk.gov.register.resources.RequestContext;

public class DownloadPageView extends ThymeleafView {

    private final Boolean downloadEnabled;

    public DownloadPageView(RequestContext requestContext, RegisterData registerData, Boolean enableDownloadResource, RegisterTrackingConfiguration registerTrackingConfiguration, RegisterResolver registerResolver) {
        super(requestContext, "download.html", registerData, registerTrackingConfiguration, registerResolver);
        this.downloadEnabled = enableDownloadResource;
    }

    public Boolean getDownloadEnabled() {
        return downloadEnabled;
    }
}
