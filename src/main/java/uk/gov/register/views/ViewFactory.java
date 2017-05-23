package uk.gov.register.views;

import org.jvnet.hk2.annotations.Service;
import uk.gov.organisation.client.GovukOrganisation;
import uk.gov.organisation.client.GovukOrganisationClient;
import uk.gov.register.configuration.*;
import uk.gov.register.core.*;
import uk.gov.register.resources.Pagination;
import uk.gov.register.resources.RequestContext;
import uk.gov.register.service.ItemConverter;
import uk.gov.register.service.RegisterLinkService;

import javax.inject.Inject;
import javax.inject.Provider;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ViewFactory {
    private final RequestContext requestContext;
    private final PublicBodiesConfiguration publicBodiesConfiguration;
    private final GovukOrganisationClient organisationClient;
    private final Provider<RegisterMetadata> registerMetadata;
    private final RegisterDomainConfiguration registerDomainConfiguration;
    private final RegisterResolver registerResolver;
    private final Provider<RegisterReadOnly> register;
    private final Provider<RegisterTrackingConfiguration> registerTrackingConfiguration;
    private final Provider<HomepageContentConfiguration> homepageContentConfiguration;
    private final Provider<ConfigManager> configManager;
    private final Provider<RegisterLinkService> registerLinkService;
    private final ItemConverter itemConverter;

    @Inject
    public ViewFactory(RequestContext requestContext,
                       PublicBodiesConfiguration publicBodiesConfiguration,
                       GovukOrganisationClient organisationClient,
                       RegisterDomainConfiguration registerDomainConfiguration,
                       Provider<HomepageContentConfiguration> homepageContentConfiguration,
                       Provider<RegisterMetadata> registerMetadata,
                       Provider<RegisterTrackingConfiguration> registerTrackingConfiguration,
                       RegisterResolver registerResolver,
                       Provider<RegisterReadOnly> register,
                       Provider<ConfigManager> configManager,
                       Provider<RegisterLinkService> registerLinkService, ItemConverter itemConverter) {
        this.requestContext = requestContext;
        this.publicBodiesConfiguration = publicBodiesConfiguration;
        this.organisationClient = organisationClient;
        this.registerDomainConfiguration = registerDomainConfiguration;
        this.registerMetadata = registerMetadata;
        this.homepageContentConfiguration = homepageContentConfiguration;
        this.registerTrackingConfiguration = registerTrackingConfiguration;
        this.registerResolver = registerResolver;
        this.register = register;
        this.configManager = configManager;
        this.registerLinkService = registerLinkService;
        this.itemConverter = itemConverter;
    }

    public ExceptionView exceptionBadRequestView(String message) {
        return exceptionView("Bad request", message);
    }

    public ExceptionView exceptionNotFoundView() {
        return exceptionView("Page not found", "If you entered a web address please check it was correct.");
    }

    public ExceptionView exceptionServerErrorView() {
        return exceptionView("Oops, looks like something went wrong", "500 error");
    }

    public ExceptionView exceptionView(String heading, String message) {
        return new ExceptionView(requestContext, heading, message, register.get(), registerTrackingConfiguration.get(), registerResolver);
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
                new HomepageContent(homepageContentConfiguration.get().getRegisterHistoryPageUrl(),
                        homepageContentConfiguration.get().getCustodianName(),
                        homepageContentConfiguration.get().getSimilarRegisters(),
                        homepageContentConfiguration.get().getIndexes()),
                registerTrackingConfiguration.get(),
                registerResolver,
                configManager.get().getFieldsConfiguration(),
                registerLinkService.get());
    }

    public DownloadPageView downloadPageView(Boolean enableDownloadResource) {
        return new DownloadPageView(requestContext, register.get(), enableDownloadResource, registerTrackingConfiguration.get(), registerResolver);
    }

    public RegisterDetailView registerDetailView(int totalRecords, int totalEntries, Optional<Instant> lastUpdated) {
        return new RegisterDetailView(totalRecords, totalEntries, lastUpdated, registerMetadata.get(),
                registerDomainConfiguration.getRegisterDomain(), homepageContentConfiguration.get().getCustodianName());
    }

    public <T> AttributionView<T> getAttributionView(String templateName, T fieldValueMap) {
        return new AttributionView<>(templateName, requestContext, getRegistry(), getBranding(), register.get(), registerTrackingConfiguration.get(), registerResolver, fieldValueMap);
    }

    public AttributionView<ItemView> getItemView(Item item) {
        return getAttributionView("item.html", getItemMediaView(item));
    }

    public AttributionView<Entry> getEntryView(Entry entry) {
        return getAttributionView("entry.html", entry);
    }

    public PaginatedView<EntryListView> getEntriesView(Collection<Entry> entries, Pagination pagination) {
        return new PaginatedView<>("entries.html", requestContext, getRegistry(), getBranding(), register.get(), registerTrackingConfiguration.get(), registerResolver, pagination, new EntryListView(entries));
    }

    public PaginatedView<EntryListView> getRecordEntriesView(String recordKey, Collection<Entry> entries, Pagination pagination) {
        return new PaginatedView<>("entries.html", requestContext, getRegistry(), getBranding(), register.get(), registerTrackingConfiguration.get(), registerResolver, pagination, new EntryListView(entries, recordKey));
    }

    public AttributionView<RecordView> getRecordView(RecordView record) {
        return getAttributionView("record.html", record);
    }

    public PaginatedView<RecordsView> getRecordsView(Pagination pagination, RecordsView recordsView) {
        return new PaginatedView<>("records.html", requestContext, getRegistry(), getBranding(), register.get(), registerTrackingConfiguration.get(), registerResolver, pagination,
                recordsView);
    }

    public ItemView getItemMediaView(Item item) {
        return new ItemView(item.getSha256hex(), itemConverter.convertItem(item), getFields());
    }

    public RecordView getRecordMediaView(Record record) {
        return new RecordView(record, getFields(), itemConverter);
    }

    public RecordsView getRecordsMediaView(List<Record> records) {
        return new RecordsView(records, getFields(), itemConverter, false, false);
    }

    public RecordsView getIndexRecordsMediaView(List<Record> records) {
        return new RecordsView(records, getFields(), itemConverter, false, true);
    }

    private PublicBody getRegistry() {
        return publicBodiesConfiguration.getPublicBody(registerMetadata.get().getRegistry());
    }

    private Optional<GovukOrganisation.Details> getBranding() {
        Optional<GovukOrganisation> organisation = organisationClient.getOrganisation(registerMetadata.get().getRegistry());
        return organisation.map(GovukOrganisation::getDetails);
    }

    private List<Field> getFields() {
        FieldsConfiguration fieldsConfiguration = configManager.get().getFieldsConfiguration();
        return register.get().getRegisterMetadata().getFields().stream()
                .map(fieldsConfiguration::getField)
                .collect(Collectors.toList());
    }
}
