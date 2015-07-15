package uk.gov.register.presentation.entity;

import uk.gov.register.presentation.view.AbstractView;

import javax.ws.rs.core.MediaType;

class JsonEntity implements Entity{
    @Override
    public Object convert(AbstractView view) {
        return view.get();
    }

    @Override
    public String contentType() {
        return MediaType.APPLICATION_JSON;
    }
}
