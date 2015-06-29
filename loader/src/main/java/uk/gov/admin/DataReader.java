package uk.gov.admin;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLConnection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DataReader {
    public final String datafile;

    public DataReader(String datafile) {
        this.datafile = datafile;
    }

    public List<String> data() throws IOException {
        try (Stream<String> data = streamData()) {
            return data.collect(Collectors.toList());
        }
    }

    public Stream<String> streamData() throws IOException {
        final URI datafileURI;
        try {
            datafileURI = datafileToURI();
        } catch (URISyntaxException e) {
            throw new RuntimeException("Could not read the datafile: " + datafile, e);
        }

        final URLConnection urlConnection = datafileURI.toURL().openConnection();

        final BufferedReader inr = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
        return inr.lines();
    }

    public URI datafileToURI() throws URISyntaxException {
        if (datafile.startsWith("/")) { // Absolute file path
            return new File(datafile).toURI();
        } else if (datafile.startsWith("http://") || datafile.startsWith("https://")) { // URL
            return new URI(datafile);
        } else { // File in current dir
            return new File(datafile).toURI();
        }
    }
}
