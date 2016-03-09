package uk.gov.indexer;

import java.io.PrintWriter;
import java.io.StringWriter;

public class ExceptionFormatter {
    public static String formatExceptionAsString(Throwable t) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        t.printStackTrace(pw);
        return sw.toString();
    }
}
