package uk.gov.register.presentation.view;

import com.fasterxml.jackson.annotation.JsonValue;
import uk.gov.register.presentation.Version;
import uk.gov.register.presentation.config.PublicBodiesConfiguration;
import uk.gov.register.presentation.resource.RequestContext;
import uk.gov.register.thymeleaf.AttributionView;

import java.util.List;

public class ListVersionView extends AttributionView {
    private final List<Version> versions;

    public ListVersionView(RequestContext requestContext, PublicBodiesConfiguration publicBodiesConfiguration, List<Version> versions) {
        super(requestContext, publicBodiesConfiguration, "history.html");
        this.versions = versions;
    }

    @JsonValue
    public List<Version> getVersions() {
        return versions;
    }
}
