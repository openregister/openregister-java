package uk.gov.register.presentation.view;

import org.jvnet.hk2.annotations.Service;
import uk.gov.register.presentation.Record;
import uk.gov.register.presentation.config.FieldsConfiguration;
import uk.gov.register.presentation.resource.RequestContext;
import uk.gov.register.thymeleaf.ThymeleafView;

import javax.inject.Inject;
import java.util.List;

@Service
public class ViewFactory {
    private final RequestContext requestContext;
    private final FieldsConfiguration fieldsConfiguration;

    @Inject
    public ViewFactory(RequestContext requestContext, FieldsConfiguration fieldsConfiguration) {
        this.requestContext = requestContext;
        this.fieldsConfiguration = fieldsConfiguration;
    }

    public SingleResultView getSingleResultView(Record record) {
        record.setFieldsConfiguration(fieldsConfiguration);
        return new SingleResultView(requestContext, record);
    }

    public ListResultView getListResultView(List<Record> allRecords) {
        allRecords.forEach(r -> r.setFieldsConfiguration(fieldsConfiguration));
        return new ListResultView(requestContext, allRecords);
    }

    public ThymeleafView thymeleafView(String templateName) {
        return new ThymeleafView(requestContext, templateName);
    }

}
