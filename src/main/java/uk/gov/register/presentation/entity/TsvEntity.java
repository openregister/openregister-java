package uk.gov.register.presentation.entity;

import org.apache.commons.lang3.StringEscapeUtils;

class TsvEntity extends CsvEntity {
    private static final String QUOTE = "\"";

    @Override
    protected String escape(String data) {
        String escapedData = StringEscapeUtils.escapeCsv(data);
        if (escapedData.contains(entrySeparator()) && escapedData.equals(StringEscapeUtils.unescapeCsv(escapedData))) {
            return QUOTE + escapedData + QUOTE;
        } else {
            return escapedData;
        }
    }

    @Override
    public String contentType() {
        return "text/tab-separated-values; charset=utf-8";
    }

    @Override
    protected String entrySeparator(){
        return "\t";
    }
}
