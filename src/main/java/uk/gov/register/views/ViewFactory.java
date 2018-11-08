package uk.gov.register.views;

import org.jvnet.hk2.annotations.Service;
import uk.gov.register.configuration.HomepageContent;
import uk.gov.register.configuration.PublicBodiesConfiguration;
import uk.gov.register.configuration.RegisterDomainConfiguration;
import uk.gov.register.core.Entry;
import uk.gov.register.core.Field;
import uk.gov.register.core.PublicBody;
import uk.gov.register.core.Record;
import uk.gov.register.core.RegisterReadOnly;
import uk.gov.register.core.RegisterResolver;
import uk.gov.register.exceptions.FieldConversionException;
import uk.gov.register.resources.Pagination;
import uk.gov.register.resources.RequestContext;
import uk.gov.register.service.ItemConverter;

import javax.inject.Inject;
import javax.inject.Provider;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Service
public class ViewFactory {
    private final RequestContext requestContext;
    private final PublicBodiesConfiguration publicBodiesConfiguration;
    private final RegisterDomainConfiguration registerDomainConfiguration;
    private final RegisterResolver registerResolver;
    private final Provider<RegisterReadOnly> register;
    private final ItemConverter itemConverter;

    @Inject
    public ViewFactory(final RequestContext requestContext,
                       final PublicBodiesConfiguration publicBodiesConfiguration,
                       final RegisterDomainConfiguration registerDomainConfiguration,
                       final RegisterResolver registerResolver,
                       final Provider<RegisterReadOnly> register,
                       final ItemConverter itemConverter) {
        this.requestContext = requestContext;
        this.publicBodiesConfiguration = publicBodiesConfiguration;
        this.registerDomainConfiguration = registerDomainConfiguration;
        this.registerResolver = registerResolver;
        this.register = register;
        this.itemConverter = itemConverter;
    }

    public ExceptionView exceptionBadRequestView(final String message) {
        return exceptionView("Bad request", message);
    }

    public ExceptionView exceptionNotFoundView() {
        return exceptionView("Page not found", "If you entered a web address please check it was correct.");
    }

    public ExceptionView exceptionFieldConversionView(final String message) {
        return exceptionView("Invalid field value", message);
    }

    public ExceptionView exceptionInconsistencyView(final String message) {
        return exceptionView("Inconsistency in the register data", message);
    }

    public ExceptionView exceptionServerErrorView() {
        return exceptionView("Oops, looks like something went wrong", "500 error");
    }

    public ExceptionView exceptionView(final String heading, final String message) {
        return new ExceptionView(requestContext, heading, message, register.get(), registerResolver);
    }

    public HomePageView homePageView(final int totalRecords, final Optional<Instant> lastUpdated) {
        return new HomePageView(
                getRegistry(),
                requestContext,
                totalRecords,
                lastUpdated,
                new HomepageContent(),
                registerResolver,
                register.get()
        );
    }

    public RegisterDetailView registerDetailView(final int totalRecords, final int totalEntries, final Optional<Instant> lastUpdated, final Optional<String> custodianName) {
        return new RegisterDetailView(totalRecords, totalEntries, lastUpdated, register.get().getRegisterMetadata(),
                registerDomainConfiguration.getRegisterDomain(), custodianName);
    }

    public <T> AttributionView<T> getAttributionView(final String templateName, final T fieldValueMap) {
        return new AttributionView<>(templateName, requestContext, getRegistry(), register.get(), registerResolver, fieldValueMap);
    }

    public <T> PaginatedView<T> getPaginatedView(final String templateName, final T view, final Pagination pagination) {
        return new PaginatedView<T>(templateName, requestContext, getRegistry(), register.get(), registerResolver, pagination, view);

    }

    public AttributionView<RecordView> getRecordView(final RecordView record) {
        return getAttributionView("record.html", record);
    }

    public PaginatedView<RecordsView> getRecordsView(final Pagination pagination, final RecordsView recordsView) {
        return new PaginatedView<>("records.html", requestContext, getRegistry(), register.get(), registerResolver, pagination,
                recordsView);
    }

    public RecordView getRecordMediaView(final Record record) throws FieldConversionException {
        return new RecordView(record, register.get().getFieldsByName(), itemConverter);
    }

    public RecordsView getRecordsMediaView(final List<Record> records) throws FieldConversionException {
        return new RecordsView(records, register.get().getFieldsByName(), itemConverter, false, false);
    }

    private PublicBody getRegistry() {
        return publicBodiesConfiguration.getPublicBody(register.get().getRegisterMetadata().getRegistry());
    }
}
