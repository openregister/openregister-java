package uk.gov.register.presentation.view;

import com.fasterxml.jackson.databind.JsonNode;
import io.dropwizard.views.View;

public abstract class AbstractView extends View {
    protected AbstractView(String templateName) {
        super(templateName);
    }

    public abstract JsonNode getJsonNode();
}


