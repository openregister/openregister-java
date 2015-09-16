package uk.gov.register.presentation.view;

import org.jvnet.hk2.annotations.Service;
import uk.gov.register.presentation.DbEntry;
import uk.gov.register.presentation.EntryConverter;
import uk.gov.register.presentation.resource.RequestContext;
import uk.gov.register.thymeleaf.ThymeleafView;

import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ViewFactory {
    private final RequestContext requestContext;
    private final EntryConverter entryConverter;

    @Inject
    public ViewFactory(RequestContext requestContext, EntryConverter entryConverter) {
        this.requestContext = requestContext;
        this.entryConverter = entryConverter;
    }

    public SingleEntryView getSingleEntryView(DbEntry dbEntry) {
        return new SingleEntryView(requestContext, entryConverter.convert(dbEntry));
    }

    public SingleEntryView getLatestEntryView(DbEntry dbEntry) {
        return new SingleEntryView(requestContext, entryConverter.convert(dbEntry), "latest-entry-of-record.html");
    }

    public ListResultView getListResultView(List<DbEntry> allDbEntries) {
        return new ListResultView(requestContext, allDbEntries.stream().map(entryConverter::convert).collect(Collectors.toList()));
    }

    public ThymeleafView thymeleafView(String templateName) {
        return new ThymeleafView(requestContext, templateName);
    }

}
