package uk.gov.register.views.representations;

import javax.ws.rs.core.MediaType;

public class ExtraMediaType {
    public static final String TEXT_HTML = "text/html; charset=UTF-8";
    public static final String TEXT_CSV = "text/csv; charset=UTF-8";
    public static final MediaType TEXT_CSV_TYPE = new MediaType("text", "csv", "UTF-8");
    public static final String APPLICATION_RSF = "application/uk-gov-rsf";
    public static final MediaType APPLICATION_RSF_TYPE = new MediaType("application", "uk-gov-rsf", "UTF-8");

    public static String transform(final String mediaType) {
        return mediaType;
    }
}
