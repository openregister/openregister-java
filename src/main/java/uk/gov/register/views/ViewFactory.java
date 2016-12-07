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
import javax.ws.rs.BadRequestException;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class ViewFactory {
    private final RequestContext requestContext;
    private final PublicBodiesConfiguration publicBodiesConfiguration;
    private final GovukOrganisationClient organisationClient;
    private final RegisterData registerData;
    private final RegisterDomainConfiguration registerDomainConfiguration;
    private final RegisterContentPages registerContentPages;
    private final RegisterResolver registerResolver;
    private RegisterTrackingConfiguration registerTrackingConfiguration;

    @Inject
    public ViewFactory(RequestContext requestContext,
                       PublicBodiesConfiguration publicBodiesConfiguration,
                       GovukOrganisationClient organisationClient,
                       RegisterDomainConfiguration registerDomainConfiguration,
                       RegisterContentPagesConfiguration registerContentPagesConfiguration,
                       RegisterData registerData,
                       RegisterTrackingConfiguration registerTrackingConfiguration,
                       RegisterResolver registerResolver) {
        this.requestContext = requestContext;
        this.publicBodiesConfiguration = publicBodiesConfiguration;
        this.organisationClient = organisationClient;
        this.registerDomainConfiguration = registerDomainConfiguration;
        this.registerData = registerData;
        this.registerContentPages = new RegisterContentPages(registerContentPagesConfiguration.getRegisterHistoryPageUrl());
        this.registerTrackingConfiguration = registerTrackingConfiguration;
        this.registerResolver = registerResolver;
    }

    public ThymeleafView thymeleafView(String templateName) {
        return new ThymeleafView(requestContext, templateName, registerData, registerTrackingConfiguration, registerResolver);
    }

    public BadRequestExceptionView badRequestExceptionView(BadRequestException e) {
        return new BadRequestExceptionView(requestContext, e, registerData, registerTrackingConfiguration, registerResolver);
    }

    public HomePageView homePageView(int totalRecords, int totalEntries, Optional<Instant> lastUpdated) {
        return new HomePageView(
                getRegistry(),
                getBranding(),
                requestContext,
                totalRecords,
                totalEntries,
                lastUpdated,
                registerData,
                registerContentPages,
                registerTrackingConfiguration,
                registerResolver);
    }

    public DownloadPageView downloadPageView(Boolean enableDownloadResource) {
        return new DownloadPageView(requestContext, registerData, enableDownloadResource, registerTrackingConfiguration, registerResolver);
    }

    public RegisterDetailView registerDetailView(int totalRecords, int totalEntries, Optional<Instant> lastUpdated) {
        return new RegisterDetailView(totalRecords, totalEntries, lastUpdated, registerData, registerDomainConfiguration.getRegisterDomain());
    }

    public <T> AttributionView<T> getAttributionView(String templateName, T fieldValueMap) {
        return new AttributionView<>(requestContext, getRegistry(), getBranding(), templateName, registerData, registerTrackingConfiguration, registerResolver, fieldValueMap);
    }

    public AttributionView<Map<String, FieldValue>> getItemView(Map<String, FieldValue> fieldValueMap) {
        return getAttributionView("item.html", fieldValueMap);
    }

    public AttributionView<Entry> getEntryView(Entry entry) {
        return getAttributionView("entry.html", entry);
    }

    public EntryListView getEntriesView(Collection<Entry> entries, Pagination pagination) {
        return new EntryListView(requestContext, pagination, getRegistry(), getBranding(), entries, registerData, registerTrackingConfiguration, registerResolver);
    }

    public EntryListView getRecordEntriesView(String recordKey, Collection<Entry> entries, Pagination pagination) {
        return new EntryListView(requestContext, pagination, getRegistry(), getBranding(), entries, recordKey, registerData, registerTrackingConfiguration, registerResolver);
    }

    public AttributionView<RecordView> getRecordView(RecordView record) {
        return getAttributionView("record.html", record);
    }

    public RecordListView getRecordListView(List<RecordView> records, Pagination pagination) {
        return new RecordListView(requestContext, getRegistry(), getBranding(), pagination, records, registerData, registerTrackingConfiguration, registerResolver);
    }

    private PublicBody getRegistry() {
        return publicBodiesConfiguration.getPublicBody(registerData.getRegister().getRegistry());
    }

    private Optional<GovukOrganisation.Details> getBranding() {
        Optional<GovukOrganisation> organisation = organisationClient.getOrganisation(registerData.getRegister().getRegistry());
        return organisation.map(GovukOrganisation::getDetails);
    }
}
