package uk.gov.indexer;

import java.io.PrintWriter;
import java.io.StringWriter;

public class ExceptionFormatter {
    public static String formatExceptionAsString(Exception e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        return sw.toString();
    }
}
