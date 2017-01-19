package uk.gov.register.protocols.classpath;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

/**
 * The presence of this class in the package corresponding to the protocol name i.e. classpath
 * under the package specified by the system property "java.protocol.handler.pkgs" allows
 * Java to read from a classpath://path url
 */
public class Handler extends URLStreamHandler {

    @Override
    protected URLConnection openConnection(URL url) throws IOException {
        return new ClasspathURLConnection(url);
    }

}
