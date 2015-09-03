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

    public static List<String> convert(DataReader reader, String type) throws IOException {
        boolean isCsv = type.equals("csv");
        CsvSchema.Builder builder = CsvSchema.builder().setColumnSeparator(isCsv ? ',' : '\t').setUseHeader(true);
        CsvSchema schema = builder.build();

        MappingIterator<Map<?, ?>> mappingIterator = new CsvMapper().reader(Map.class).withSchema(schema).readValues(reader.reader());
        List<String> jsons = new ArrayList<>();
        while (mappingIterator.hasNext()) {
            String entryJson = objectMapper.writeValueAsString(mappingIterator.next());
            jsons.add(entryJson);
        }

        return jsons;
    }
}
