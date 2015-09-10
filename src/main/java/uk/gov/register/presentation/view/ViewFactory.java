package uk.gov.register.presentation.view;

import org.jvnet.hk2.annotations.Service;
import uk.gov.register.presentation.DbRecord;
import uk.gov.register.presentation.RecordConverter;
import uk.gov.register.presentation.resource.RequestContext;
import uk.gov.register.thymeleaf.ThymeleafView;

import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ViewFactory {
    private final RequestContext requestContext;
    private final RecordConverter recordConverter;

    @Inject
    public ViewFactory(RequestContext requestContext, RecordConverter recordConverter) {
        this.requestContext = requestContext;
        this.recordConverter = recordConverter;
    }

    public SingleResultView getSingleResultView(DbRecord dbRecord) {
        return new SingleResultView(requestContext, recordConverter.convert(dbRecord));
    }

    public ListResultView getListResultView(List<DbRecord> allDbRecords) {
        return new ListResultView(requestContext, allDbRecords.stream().map(recordConverter::convert).collect(Collectors.toList()));
    }

    public ThymeleafView thymeleafView(String templateName) {
        return new ThymeleafView(requestContext, templateName);
    }

}
