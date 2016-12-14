package uk.gov.register.views;

import org.jvnet.hk2.annotations.Service;
import uk.gov.organisation.client.GovukOrganisation;
import uk.gov.organisation.client.GovukOrganisationClient;
import uk.gov.register.configuration.*;
import uk.gov.register.core.*;
import uk.gov.register.resources.Pagination;
import uk.gov.register.resources.RequestContext;
import uk.gov.register.thymeleaf.ThymeleafView;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.ws.rs.BadRequestException;
import java.time.Instant;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

@Service
public class ViewFactory {
    private final RequestContext requestContext;
    private final PublicBodiesConfiguration publicBodiesConfiguration;
    private final GovukOrganisationClient organisationClient;
    private final Provider<RegisterMetadata> registerMetadata;
    private final RegisterDomainConfiguration registerDomainConfiguration;
    private final RegisterContentPages registerContentPages;
    private final RegisterResolver registerResolver;
    private final Provider<RegisterReadOnly> register;
    private RegisterTrackingConfiguration registerTrackingConfiguration;

    @Inject
    public ViewFactory(RequestContext requestContext,
                       PublicBodiesConfiguration publicBodiesConfiguration,
                       GovukOrganisationClient organisationClient,
                       RegisterDomainConfiguration registerDomainConfiguration,
                       RegisterContentPagesConfiguration registerContentPagesConfiguration,
                       Provider<RegisterMetadata> registerMetadata,
                       RegisterTrackingConfiguration registerTrackingConfiguration,
                       RegisterResolver registerResolver,
                       Provider<RegisterReadOnly> register) {
        this.requestContext = requestContext;
        this.publicBodiesConfiguration = publicBodiesConfiguration;
        this.organisationClient = organisationClient;
        this.registerDomainConfiguration = registerDomainConfiguration;
        this.registerMetadata = registerMetadata;
        this.registerContentPages = new RegisterContentPages(registerContentPagesConfiguration.getRegisterHistoryPageUrl());
        this.registerTrackingConfiguration = registerTrackingConfiguration;
        this.registerResolver = registerResolver;
        this.register = register;
    }

    public ThymeleafView thymeleafView(String templateName) {
        return new ThymeleafView(requestContext, templateName, registerTrackingConfiguration, registerResolver, register.get());
    }

    public BadRequestExceptionView badRequestExceptionView(BadRequestException e) {
        return new BadRequestExceptionView(requestContext, e, register.get(), registerTrackingConfiguration, registerResolver);
    }

    public HomePageView homePageView(int totalRecords, int totalEntries, Optional<Instant> lastUpdated) {
        return new HomePageView(
                getRegistry(),
                getBranding(),
                requestContext,
                totalRecords,
                totalEntries,
                lastUpdated,
                register.get(),
                registerContentPages,
                registerTrackingConfiguration,
                registerResolver);
    }

    public DownloadPageView downloadPageView(Boolean enableDownloadResource) {
        return new DownloadPageView(requestContext, register.get(), enableDownloadResource, registerTrackingConfiguration, registerResolver);
    }

    public RegisterDetailView registerDetailView(int totalRecords, int totalEntries, Optional<Instant> lastUpdated) {
        return new RegisterDetailView(totalRecords, totalEntries, lastUpdated, registerMetadata.get(), registerDomainConfiguration.getRegisterDomain());
    }

    public <T> AttributionView<T> getAttributionView(String templateName, T fieldValueMap) {
        return new AttributionView<>(templateName, requestContext, getRegistry(), getBranding(), register.get(), registerTrackingConfiguration, registerResolver, fieldValueMap);
    }

    public AttributionView<Map<String, FieldValue>> getItemView(Map<String, FieldValue> fieldValueMap) {
        return getAttributionView("item.html", fieldValueMap);
    }

    public AttributionView<Entry> getEntryView(Entry entry) {
        return getAttributionView("entry.html", entry);
    }

    public PaginatedView<EntryListView> getEntriesView(Collection<Entry> entries, Pagination pagination) {
        return new PaginatedView<>("entries.html", requestContext, getRegistry(), getBranding(), register.get(), registerTrackingConfiguration, registerResolver, pagination, new EntryListView(entries));
    }

    public PaginatedView<EntryListView> getRecordEntriesView(String recordKey, Collection<Entry> entries, Pagination pagination) {
        return new PaginatedView<>("entries.html", requestContext, getRegistry(), getBranding(), register.get(), registerTrackingConfiguration, registerResolver, pagination, new EntryListView(entries, recordKey));
    }

    public AttributionView<RecordView> getRecordView(RecordView record) {
        return getAttributionView("record.html", record);
    }

    public PaginatedView<RecordsView> getRecordListView(Pagination pagination, RecordsView recordsView) {
        return new PaginatedView<>("records.html", requestContext, getRegistry(), getBranding(), register.get(), registerTrackingConfiguration, registerResolver, pagination,
                recordsView);
    }

    private PublicBody getRegistry() {
        return publicBodiesConfiguration.getPublicBody(registerMetadata.get().getRegistry());
    }

    private Optional<GovukOrganisation.Details> getBranding() {
        Optional<GovukOrganisation> organisation = organisationClient.getOrganisation(registerMetadata.get().getRegistry());
        return organisation.map(GovukOrganisation::getDetails);
    }
}
