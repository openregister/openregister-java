package uk.gov.register.views;

import com.google.common.collect.Iterables;
import uk.gov.organisation.client.GovukOrganisation;
import uk.gov.register.configuration.FieldsConfiguration;
import uk.gov.register.configuration.HomepageContent;
import uk.gov.register.configuration.RegisterTrackingConfiguration;
import uk.gov.register.core.*;
import uk.gov.register.resources.RequestContext;

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
    private final HomepageContent homepageContent;
    private final Iterable<Field> fields;

    public HomePageView(
            PublicBody registry,
            Optional<GovukOrganisation.Details> registryBranding,
            RequestContext requestContext,
            int totalRecords,
            int totalEntries,
            Optional<Instant> lastUpdated,
            RegisterReadOnly register,
            HomepageContent homepageContent,
            RegisterTrackingConfiguration registerTrackingConfiguration,
            RegisterResolver registerResolver,
            FieldsConfiguration fieldsConfiguration) {
        super("home.html", requestContext, registry, registryBranding, register, registerTrackingConfiguration, registerResolver, null);
        this.totalRecords = totalRecords;
        this.totalEntries = totalEntries;
        this.lastUpdated = lastUpdated;
        this.homepageContent = homepageContent;
        this.fields = Iterables.transform(getRegister().getFields(), f -> fieldsConfiguration.getField(f));
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
}
