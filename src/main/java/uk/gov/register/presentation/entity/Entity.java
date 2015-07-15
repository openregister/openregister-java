package uk.gov.register.presentation.entity;

import uk.gov.register.presentation.view.AbstractView;

public interface Entity{
    Object convert(AbstractView view);
    String contentType();
}
