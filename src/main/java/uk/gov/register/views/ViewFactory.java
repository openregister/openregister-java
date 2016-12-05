package uk.gov.register.views;

import org.jvnet.hk2.annotations.Service;
import uk.gov.organisation.client.GovukOrganisation;
import uk.gov.organisation.client.GovukOrganisationClient;
import uk.gov.register.configuration.*;
import uk.gov.register.core.*;
import uk.gov.register.resources.Pagination;
import uk.gov.register.resources.RequestContext;
import uk.gov.register.service.ItemConverter;
import uk.gov.register.thymeleaf.ThymeleafView;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.ws.rs.BadRequestException;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Service
public class ViewFactory {
    private final RequestContext requestContext;
    private final ItemConverter itemConverter;
    private final PublicBodiesConfiguration publicBodiesConfiguration;
    private final GovukOrganisationClient organisationClient;
    private final Provider<RegisterData> registerData;
    private final RegisterDomainConfiguration registerDomainConfiguration;
    private final RegisterContentPages registerContentPages;
    private final RegisterResolver registerResolver;
    private RegisterTrackingConfiguration registerTrackingConfiguration;

    @Inject
    public ViewFactory(RequestContext requestContext,
                       ItemConverter itemConverter,
                       PublicBodiesConfiguration publicBodiesConfiguration,
                       GovukOrganisationClient organisationClient,
                       RegisterDomainConfiguration registerDomainConfiguration,
                       RegisterContentPagesConfiguration registerContentPagesConfiguration,
                       Provider<RegisterData> registerData,
                       RegisterTrackingConfiguration registerTrackingConfiguration,
                       RegisterResolver registerResolver) {
        this.requestContext = requestContext;
        this.itemConverter = itemConverter;
        this.publicBodiesConfiguration = publicBodiesConfiguration;
        this.organisationClient = organisationClient;
        this.registerDomainConfiguration = registerDomainConfiguration;
        this.registerData = registerData;
        this.registerContentPages = new RegisterContentPages(registerContentPagesConfiguration.getRegisterHistoryPageUrl());
        this.registerTrackingConfiguration = registerTrackingConfiguration;
        this.registerResolver = registerResolver;
    }

    public ThymeleafView thymeleafView(String templateName) {
        return new ThymeleafView(requestContext, templateName, registerData.get(), registerTrackingConfiguration, registerResolver);
    }

    public BadRequestExceptionView badRequestExceptionView(BadRequestException e) {
        return new BadRequestExceptionView(requestContext, e, registerData.get(), registerTrackingConfiguration, registerResolver);
    }

    public HomePageView homePageView(int totalRecords, int totalEntries, Optional<Instant> lastUpdated) {
        return new HomePageView(
                getRegistry(),
                getBranding(),
                requestContext,
                totalRecords,
                totalEntries,
                lastUpdated,
                registerData.get(),
                registerContentPages,
                registerTrackingConfiguration,
                registerResolver);
    }

    public DownloadPageView downloadPageView(Boolean enableDownloadResource) {
        return new DownloadPageView(requestContext, registerData.get(), enableDownloadResource, registerTrackingConfiguration, registerResolver);
    }

    public RegisterDetailView registerDetailView(int totalRecords, int totalEntries, Optional<Instant> lastUpdated) {
        return new RegisterDetailView(totalRecords, totalEntries, lastUpdated, registerData.get(), registerDomainConfiguration.getRegisterDomain());
    }

    public ItemView getItemView(Item item) {
        return new ItemView(requestContext, getRegistry(), getBranding(), itemConverter, item, registerData.get(), registerTrackingConfiguration, registerResolver);
    }

    public EntryView getEntryView(Entry entry) {
        return new EntryView(requestContext, getRegistry(), getBranding(), entry, registerData.get(), registerTrackingConfiguration, registerResolver);
    }

    public EntryListView getEntriesView(Collection<Entry> entries, Pagination pagination) {
        return new EntryListView(requestContext, pagination, getRegistry(), getBranding(), entries, registerData.get(), registerTrackingConfiguration, registerResolver);
    }

    public EntryListView getRecordEntriesView(String recordKey, Collection<Entry> entries, Pagination pagination) {
        return new EntryListView(requestContext, pagination, getRegistry(), getBranding(), entries, recordKey, registerData.get(), registerTrackingConfiguration, registerResolver);
    }

    public RecordView getRecordView(Record record) {
        return new RecordView(requestContext, getRegistry(), getBranding(), itemConverter, record, registerData.get(), registerTrackingConfiguration, registerResolver);
    }

    public RecordListView getRecordListView(List<Record> records, Pagination pagination) {
        return new RecordListView(requestContext, getRegistry(), getBranding(), pagination, itemConverter, records, registerData.get(), registerTrackingConfiguration, registerResolver);
    }

    private PublicBody getRegistry() {
        return publicBodiesConfiguration.getPublicBody(registerData.get().getRegister().getRegistry());
    }

    private Optional<GovukOrganisation.Details> getBranding() {
        Optional<GovukOrganisation> organisation = organisationClient.getOrganisation(registerData.get().getRegister().getRegistry());
        return organisation.map(GovukOrganisation::getDetails);
    }
}
