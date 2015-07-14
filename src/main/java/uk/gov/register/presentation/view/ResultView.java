package uk.gov.register.presentation.view;

import uk.gov.register.presentation.mapper.JsonObjectMapper;

import java.util.Map;

public class ResultView extends AbstractView {
    private final Object object;

    public ResultView(String templateName, Object object) {
        super(templateName);
        this.object = object;
    }

    @Override
    public Object getObject() {
        return JsonObjectMapper.convert(object, Map.class);
    }
}