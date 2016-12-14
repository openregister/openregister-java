package uk.gov.register.protocols.s3;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

/**
 * The presence of this class in the package corresponding to the protocol name i.e. s3
 * under the package specified by the system property "java.protocol.handler.pkgs" allows
 * Java to read from a s3://bucket/key url
 */
public class Handler extends URLStreamHandler {

    @Override
    protected URLConnection openConnection(URL url) throws IOException {
        return new S3URLConnection(url);
    }

}
