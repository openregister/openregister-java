package uk.gov.admin;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLConnection;
import java.util.stream.Stream;

public class DataReader {
    public final String datafile;

    public DataReader(String datafile) {
        this.datafile = datafile;
    }

    public String data() {
        try {
            final URI datafileURI = datafileToURI();
            final String data = readDataFromURI(datafileURI);

            return data;
        } catch (URISyntaxException | IOException e) {
            e.printStackTrace();
        }

        return null;
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

    private String readDataFromURI(URI datafileURI) throws IOException {
        final URLConnection urlConnection = datafileURI.toURL().openConnection();
        final int contentLength = urlConnection.getContentLength();

        final ByteArrayOutputStream data = new ByteArrayOutputStream(contentLength);
        byte[] buf = new byte[1024];
        int totalBytesRead = 0, bytesRead = 0;
        final InputStream urlIn = urlConnection.getInputStream();
        while((bytesRead = urlIn.read(buf)) > 0) {
            totalBytesRead += bytesRead;
            data.write(buf, 0, bytesRead);
        }
        if(totalBytesRead != contentLength) {
            throw new RuntimeException("Error reading data from datafile: " + datafile);
        }

        final byte[] bytes = data.toByteArray();
        return new String(bytes);
    }

    private URI datafileToURI() throws URISyntaxException {
        if (datafile.startsWith("/")) { // Absolute file path
            return new File(datafile).toURI();
        } else if (datafile.startsWith("http://")) { // URL
            return new URI(datafile);
        } else { // File in current dir
            return new File(datafile).toURI();
        }
    }
}
