package uk.gov.register.presentation.representations;

import javax.ws.rs.core.MediaType;

public class ExtraMediaType {
    public static final String TEXT_CSV = "text/csv; charset=utf-8";
    public static final MediaType TEXT_CSV_TYPE = new MediaType("text", "csv", "utf-8");
    public static final String TEXT_TSV = "text/tab-separated-values; charset=utf-8";
    public static final MediaType TEXT_TSV_TYPE = new MediaType("text", "tab-separated-values", "utf-8");
}
