package uk.gov.admin;

import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import org.codehaus.jackson.map.MappingIterator;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.Iterator;
import java.util.Map;

public class DataFileReader {
    private final String datafile;
    private final String type;

    public DataFileReader(String datafile, String type) {
        this.datafile = datafile;
        this.type = type;
    }

    public Iterator<String> getFileEntriesIterator() throws IOException {
        switch (type) {
            case "jsonl":
                return reader().lines().iterator();
            case "csv":
                return csvIterator(',');
            default:
                return csvIterator('\t');
        }
    }

    private Iterator<String> csvIterator(char separator) throws IOException {
        CsvSchema schema = CsvSchema.builder()
                .setColumnSeparator(separator)
                .setUseHeader(true)
                .build();

        MappingIterator<String> mappingIterator = new CsvMapper().reader(Map.class)
                .withSchema(schema)
                .readValues(reader());

        return new CsvEntriesIterator(mappingIterator);
    }

    private BufferedReader reader() {
        try {
            URI uri = datafile.startsWith("http://") || datafile.startsWith("https://") ? new URI(datafile) : new File(datafile).toURI();
            InputStreamReader inputStreamReader = new InputStreamReader(uri.toURL().openStream());
            return new BufferedReader(inputStreamReader);
        } catch (Exception e) {
            throw new RuntimeException("Error creating stream to read data to load", e);
        }
    }

}
