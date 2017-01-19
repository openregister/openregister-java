package uk.gov.register.protocols.classpath;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ClasspathURLConnection extends URLConnection {

    private final Pattern classpathPattern = Pattern.compile("classpath://(.+)");

    ClasspathURLConnection(URL url) {
        super(url);
    }

    @Override
    public InputStream getInputStream() throws IOException {
        String path = getPath(url);
        return this.getClass().getClassLoader().getResourceAsStream(path);
    }

    private String getPath(URL url) throws MalformedURLException {
        Matcher matcher = classpathPattern.matcher(url.toString());
        if (matcher.matches() && matcher.groupCount() == 1) {
            return matcher.group(1);
        }
        throw new MalformedURLException("url did not match pattern for classpath url: " + url.toString());
    }

    @Override
    public void connect() throws IOException {
        // do nothing
    }
}
