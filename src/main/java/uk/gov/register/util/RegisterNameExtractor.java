package uk.gov.register.util;

public class RegisterNameExtractor {
    public static String extractRegisterName(String host) {
        return host.split(":")[0].split("\\.")[0];
    }
}
