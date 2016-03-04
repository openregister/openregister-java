package uk.gov.register.presentation.view;

import org.markdownj.MarkdownProcessor;
import uk.gov.register.presentation.LinkValue;
import uk.gov.register.presentation.config.PublicBody;
import uk.gov.register.presentation.resource.RequestContext;
import uk.gov.organisation.client.GovukOrganisation;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class HomePageView extends AttributionView {
    private final static DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("d MMM uuuu").withZone(ZoneId.of("UTC"));

    private final MarkdownProcessor markdownProcessor = new MarkdownProcessor();

    private final Instant lastUpdated;
    private final int totalRecords;
    private final int totalEntries;
    private final String registerDomain;

    public HomePageView(
            PublicBody custodian,
            Optional<GovukOrganisation.Details> custodianBranding,
            RequestContext requestContext,
            int totalRecords,
            int totalEntries,
            Instant lastUpdated,
            String registerDomain
    ) {
        super(requestContext, custodian, custodianBranding, "home.html");
        this.totalRecords = totalRecords;
        this.totalEntries = totalEntries;
        this.lastUpdated = lastUpdated;
        this.registerDomain = registerDomain;
    }

    @SuppressWarnings("unused, used from template")
    public String getRegisterText() {
        return markdownProcessor.markdown(getRegister().getText());
    }

    @SuppressWarnings("unused, used from template")
    public int getTotalRecords() {
        return totalRecords;
    }

    @SuppressWarnings("unused, used from template")
    public int getTotalEntries() {
        return totalEntries;
    }

    @SuppressWarnings("unused, used from template")
    public String getLastUpdatedTime() {
        return DATE_TIME_FORMATTER.format(lastUpdated);
    }

    @SuppressWarnings("unused, used from template")
    public String getLinkToRegisterRegister() {
        return new LinkValue("register", registerDomain, getRegisterId()).link();
    }
}
