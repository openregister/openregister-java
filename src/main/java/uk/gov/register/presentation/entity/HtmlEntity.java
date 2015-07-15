package uk.gov.register.presentation.entity;

import uk.gov.register.presentation.view.AbstractView;

import javax.ws.rs.core.MediaType;

class HtmlEntity implements Entity{
    @Override
    public Object convert(AbstractView view) {
        return view;
    }

    @Override
    public String contentType() {
        return MediaType.TEXT_HTML;
    }
}
