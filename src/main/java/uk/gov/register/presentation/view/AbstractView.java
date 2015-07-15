package uk.gov.register.presentation.view;

import io.dropwizard.views.View;

public abstract class AbstractView<T> extends View {
    protected AbstractView(String templateName) {
        super(templateName);
    }

    public abstract T get();
}


