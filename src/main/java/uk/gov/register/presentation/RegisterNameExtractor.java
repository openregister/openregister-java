package uk.gov.register.presentation;

import org.apache.commons.lang3.StringUtils;

public class RegisterNameExtractor {
    @SuppressWarnings("unused, Used from templates")
    public static String capitalizedRegisterName(String host) {
        return StringUtils.capitalize(extractRegisterName(host));
    }

    public static String extractRegisterName(String host) {
        return host.split(":")[0].split("\\.")[0];
    }
}
