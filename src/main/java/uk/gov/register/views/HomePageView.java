package uk.gov.register.views;

import uk.gov.organisation.client.GovukOrganisation;
import uk.gov.register.configuration.RegisterContentPages;
import uk.gov.register.configuration.RegisterDomainConfiguration;
import uk.gov.register.configuration.RegisterTrackingConfiguration;
import uk.gov.register.core.PublicBody;
import uk.gov.register.core.RegisterData;
import uk.gov.register.resources.RequestContext;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class HomePageView extends AttributionView {
    /* Formatter for friendly (ie human-readable) dates
       based on guidance at https://www.gov.uk/guidance/style-guide/a-to-z-of-gov-uk-style#dates
     */
    private final static DateTimeFormatter FRIENDLY_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("d MMMM uuuu").withZone(ZoneId.of("UTC"));

    private final Optional<Instant> lastUpdated;
    private final int totalRecords;
    private final int totalEntries;
    private final String registerDomain;
    private final RegisterContentPages registerContentPages;

    public HomePageView(
            PublicBody custodian,
            Optional<GovukOrganisation.Details> custodianBranding,
            RequestContext requestContext,
            int totalRecords,
            int totalEntries,
            Optional<Instant> lastUpdated,
            RegisterDomainConfiguration registerDomainConfiguration,
            RegisterData registerData,
            RegisterContentPages registerContentPages,
            RegisterTrackingConfiguration registerTrackingConfiguration) {
        super(requestContext, custodian, custodianBranding, "home.html", registerDomainConfiguration, registerData, registerTrackingConfiguration);
        this.totalRecords = totalRecords;
        this.totalEntries = totalEntries;
        this.lastUpdated = lastUpdated;
        this.registerDomain = registerDomainConfiguration.getRegisterDomain();
        this.registerContentPages = registerContentPages;
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
        return lastUpdated.isPresent() ? FRIENDLY_DATE_TIME_FORMATTER.format(lastUpdated.get()) : "";
    }

    @SuppressWarnings("unused, used from template")
    public String getLinkToRegisterRegister() {
        return String.format("%1$s://register.%2$s/record/%3$s",
                getScheme(),
                registerDomain,
                getRegisterId()
        );
    }

    @SuppressWarnings("unused, used from template")
    public RegisterContentPages getContentPages() {
        return registerContentPages;
    }
}
