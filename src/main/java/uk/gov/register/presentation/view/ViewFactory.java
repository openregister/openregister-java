package uk.gov.register.presentation.view;

import org.jvnet.hk2.annotations.Service;
import uk.gov.register.presentation.DbEntry;
import uk.gov.register.presentation.EntryConverter;
import uk.gov.register.presentation.config.PublicBodiesConfiguration;
import uk.gov.register.presentation.resource.Pagination;
import uk.gov.register.presentation.resource.RequestContext;
import uk.gov.register.thymeleaf.HomePageView;
import uk.gov.register.thymeleaf.ThymeleafView;

import javax.inject.Inject;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ViewFactory {
    private final RequestContext requestContext;
    private final EntryConverter entryConverter;
    private final PublicBodiesConfiguration publicBodiesConfiguration;

    @Inject
    public ViewFactory(RequestContext requestContext, EntryConverter entryConverter, PublicBodiesConfiguration publicBodiesConfiguration) {
        this.requestContext = requestContext;
        this.entryConverter = entryConverter;
        this.publicBodiesConfiguration = publicBodiesConfiguration;
    }

    public SingleEntryView getSingleEntryView(DbEntry dbEntry) {
        return new SingleEntryView(requestContext, entryConverter.convert(dbEntry));
    }

    public SingleEntryView getLatestEntryView(DbEntry dbEntry) {
        return new SingleEntryView(requestContext, entryConverter.convert(dbEntry), "latest-entry-of-record.html");
    }

    public EntryListView getEntryFeedView(List<DbEntry> allDbEntries, Pagination pagination) {
        return new EntryListView(requestContext,
                allDbEntries.stream().map(entryConverter::convert).collect(Collectors.toList()),
                pagination,
                "feed.html"
        );
    }

    public EntryListView getRecordEntriesView(List<DbEntry> allDbEntries, Pagination pagination) {
        return new EntryListView(requestContext,
                allDbEntries.stream().map(entryConverter::convert).collect(Collectors.toList()),
                pagination,
                "current.html"
        );
    }

    public ThymeleafView thymeleafView(String templateName) {
        return new ThymeleafView(requestContext, templateName);
    }

    public HomePageView homePageView(int totalRecords, LocalDateTime lastUpdated) {
        return new HomePageView(publicBodiesConfiguration, requestContext, totalRecords, lastUpdated);
    }

}
