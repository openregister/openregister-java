package uk.gov.admin;

import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import org.codehaus.jackson.map.MappingIterator;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ToJSONLConverter {
    static ObjectMapper objectMapper = new ObjectMapper();


    public static List<String> convertCsvToJsonl(DataReader reader) throws IOException {
        return convert(reader, CsvSchema.builder().setColumnSeparator(',').setUseHeader(true).build());
    }

    public static List<String> convertTsvToJsonl(DataReader reader) throws IOException {
        return convert(reader, CsvSchema.builder().setColumnSeparator('\t').setUseHeader(true).build());
    }

    private static List<String> convert(DataReader reader, CsvSchema schema) throws IOException {
        MappingIterator<Map<?, ?>> mappingIterator = new CsvMapper().reader(Map.class).withSchema(schema).readValues(reader.reader());
        List<String> jsons = new ArrayList<>();
        while (mappingIterator.hasNext()) {
            String entryJson = objectMapper.writeValueAsString(mappingIterator.next());
            jsons.add(entryJson);
        }

        return jsons;
    }
}
