package uk.gov.register.views;

import uk.gov.organisation.client.GovukOrganisation;
import uk.gov.register.configuration.HomepageContent;
import uk.gov.register.core.*;
import uk.gov.register.resources.RequestContext;

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
    private final HomepageContent homepageContent;

    public HomePageView(
            final PublicBody registry,
            final Optional<GovukOrganisation.Details> registryBranding,
            final RequestContext requestContext,
            final int totalRecords,
            final Optional<Instant> lastUpdated,
            final HomepageContent homepageContent,
            final RegisterResolver registerResolver,
            final RegisterReadOnly register) {
        super("home.html", requestContext, registry, registryBranding, register, registerResolver, null);
        this.totalRecords = totalRecords;
        this.lastUpdated = lastUpdated;
        this.homepageContent = homepageContent;
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
    public String getLastUpdatedTime() {
        return lastUpdated.isPresent() ? FRIENDLY_DATE_TIME_FORMATTER.format(lastUpdated.get()) : "";
    }

    @SuppressWarnings("unused, used from template")
    public HomepageContent getHomepageContent() {
        return homepageContent;
    }
}
