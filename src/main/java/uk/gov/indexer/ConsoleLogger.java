package uk.gov.indexer;

import java.time.LocalDateTime;

public class ConsoleLogger {
    public static void log(String message) {
        System.out.println(LocalDateTime.now() + ": " + message);
    }
}
