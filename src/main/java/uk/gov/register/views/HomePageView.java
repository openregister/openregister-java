package uk.gov.register.views;

import uk.gov.organisation.client.GovukOrganisation;
import uk.gov.register.configuration.HomepageContent;
import uk.gov.register.configuration.RegisterTrackingConfiguration;
import uk.gov.register.core.*;
import uk.gov.register.resources.RequestContext;
import uk.gov.register.service.RegisterLinkService;

import java.net.URI;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class HomePageView extends AttributionView<Object> {
    /* Formatter for friendly (ie human-readable) dates
       based on guidance at https://www.gov.uk/guidance/style-guide/a-to-z-of-gov-uk-style#dates
     */
    private final static DateTimeFormatter FRIENDLY_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("d MMMM uuuu").withZone(ZoneId.of("UTC"));

    private final Optional<Instant> lastUpdated;
    private final int totalRecords;
    private final int totalEntries;
    private final Optional<String> custodianName;
    private final HomepageContent homepageContent;
    private final Iterable<Field> fields;
    private final RegisterLinks registerLinks;

    public HomePageView(
            final PublicBody registry,
            final Optional<GovukOrganisation.Details> registryBranding,
            final RequestContext requestContext,
            final int totalRecords,
            final int totalEntries,
            final Optional<Instant> lastUpdated,
            final Optional<String> custodianName,
            final HomepageContent homepageContent,
            final RegisterTrackingConfiguration registerTrackingConfiguration,
            final RegisterResolver registerResolver,
            final Iterable<Field> fields,
            final RegisterLinkService registerLinkService,
            final RegisterReadOnly register) {
        super("home.html", requestContext, registry, registryBranding, register, registerTrackingConfiguration, registerResolver, null);
        this.totalRecords = totalRecords;
        this.totalEntries = totalEntries;
        this.lastUpdated = lastUpdated;
        this.custodianName = custodianName;
        this.homepageContent = homepageContent;
        this.fields = fields;
        registerLinks = registerLinkService.getRegisterLinks(getRegisterId());
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
    public Optional<String> getCustodianName() {
        return custodianName;
    }

    @SuppressWarnings("unused, used from template")
    public URI getLinkToRegisterRegister() {
        return getLinkResolver().resolve(new RegisterName("register"), getRegisterId().value());
    }

    @SuppressWarnings("unused, used from template")
    public HomepageContent getHomepageContent() {
        return homepageContent;
    }

    public Iterable<Field> getFields() {
        return fields;
    }

    public RegisterLinks getRegisterLinks() {
        return registerLinks;
    }
}
