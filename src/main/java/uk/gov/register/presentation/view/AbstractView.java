package uk.gov.register.presentation.view;

import io.dropwizard.views.View;

import java.util.Map;
import java.util.Set;

public abstract class AbstractView extends View {
    protected AbstractView(String templateName) {
        super(templateName);
    }

    public abstract Object getObject();

    public abstract ResultView flatten();
}


