package uk.gov.admin;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URI;

/**
 * Do not use in multi-threaded environment without external guards
 * Do not reuse
 */
public class DataReader {
    public final String datafile;

    public DataReader(String datafile) {
        this.datafile = datafile;
    }

    public BufferedReader reader() {
        try {
            URI uri = datafile.startsWith("http://") || datafile.startsWith("https://") ? new URI(datafile) : new File(datafile).toURI();
            InputStreamReader inputStreamReader = new InputStreamReader(uri.toURL().openStream());
            return new BufferedReader(inputStreamReader);
        } catch (Exception e) {
            throw new RuntimeException("Error creating stream to read data to load", e);
        }
    }

}
