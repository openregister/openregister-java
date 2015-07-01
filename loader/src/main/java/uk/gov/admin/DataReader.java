package uk.gov.admin;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.stream.Stream;

/**
 * Do not use in multi-threaded environment without external guards
 * Do not reuse
 */
public class DataReader {
    public final String datafile;
    private BufferedReader in;

    public DataReader(String datafile) {
        this.datafile = datafile;
    }

    public Stream<String> streamData() {
        return reader().lines();
    }

    public BufferedReader reader() {
        try {
            URI uri = getUri();
            InputStreamReader inputStreamReader = new InputStreamReader(uri.toURL().openStream());
            in = new BufferedReader(inputStreamReader);

            return in;
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException("Error creating stream to read data to load", e);
        }
    }

    private URI getUri() throws URISyntaxException {
        URI uri;
        if (datafile.startsWith("/")) { // Absolute file path
            uri = new File(datafile).toURI();
        } else if (datafile.startsWith("http://") || datafile.startsWith("https://")) { // URL
            uri = new URI(datafile);
        } else { // File in current dir
            uri = new File(datafile).toURI();
        }
        return uri;
    }
}
