package uk.gov.register.views;

import uk.gov.register.configuration.RegisterDomainConfiguration;
import uk.gov.register.configuration.RegisterTrackingConfiguration;
import uk.gov.register.core.RegisterData;
import uk.gov.register.thymeleaf.ThymeleafView;
import uk.gov.register.resources.RequestContext;

public class DownloadPageView extends ThymeleafView {

    private final Boolean downloadEnabled;

    public DownloadPageView(RequestContext requestContext, RegisterDomainConfiguration registerDomainConfiguration, RegisterData registerData, Boolean enableDownloadResource, RegisterTrackingConfiguration registerTrackingConfiguration) {
        super(requestContext, "download.html", registerData, registerDomainConfiguration, registerTrackingConfiguration);
        this.downloadEnabled = enableDownloadResource;
    }

    public Boolean getDownloadEnabled() {
        return downloadEnabled;
    }
}
