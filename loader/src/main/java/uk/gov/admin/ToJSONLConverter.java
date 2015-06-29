package uk.gov.admin;

import com.univocity.parsers.common.processor.RowListProcessor;
import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;
import com.univocity.parsers.tsv.TsvParser;
import com.univocity.parsers.tsv.TsvParserSettings;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ToJSONLConverter {
    private final static ToJSONLConverter identity =
            new ToJSONLConverter(ConvertableType.jsonl) {
                @Override
                public List<String> convert(DataReader reader) throws URISyntaxException, IOException {
                    return reader.streamData().collect(Collectors.toList());
                }
            };
    private final ConvertableType convertableType;

    private ToJSONLConverter(ConvertableType theConvertableType) {
        this.convertableType = theConvertableType;
    }

    public static ToJSONLConverter converterFor(ConvertableType convertableType) {
        if (convertableType == ConvertableType.jsonl) return identity;

        return new ToJSONLConverter(convertableType);
    }

    public List<String> convert(DataReader reader) throws URISyntaxException, IOException {
        RowListProcessor rowProcessor;
        if (convertableType == ConvertableType.tsv)
            rowProcessor = getTsvParser(reader);
        else
            rowProcessor = getCsvParser(reader);

        String[] headers = rowProcessor.getHeaders();
        List<String[]> rows = rowProcessor.getRows();

        return convertRecordsToJsonl(headers, rows);
    }

    private RowListProcessor getCsvParser(DataReader reader) throws IOException, URISyntaxException {
        CsvParserSettings parserSettings = new CsvParserSettings();
        parserSettings.setLineSeparatorDetectionEnabled(true);
        RowListProcessor rowProcessor = new RowListProcessor();
        parserSettings.setRowProcessor(rowProcessor);
        parserSettings.setHeaderExtractionEnabled(true);
        CsvParser parser = new CsvParser(parserSettings);
        parser.parse(new InputStreamReader(reader.datafileToURI().toURL().openStream()));

        return rowProcessor;
    }

    private RowListProcessor getTsvParser(DataReader reader) throws IOException, URISyntaxException {
        TsvParserSettings parserSettings = new TsvParserSettings();
        parserSettings.setLineSeparatorDetectionEnabled(true);
        RowListProcessor rowProcessor = new RowListProcessor();
        parserSettings.setRowProcessor(rowProcessor);
        parserSettings.setHeaderExtractionEnabled(true);
        TsvParser parser = new TsvParser(parserSettings);
        parser.parse(new InputStreamReader(reader.datafileToURI().toURL().openStream()));

        return rowProcessor;
    }

    private List<String> convertRecordsToJsonl(String[] headers, List<String[]> rows) {
        List<String> jsonlDocs = new ArrayList<>();
        for (String[] fields : rows) {
            String jsonl = "{";
            for (int i = 0; i < headers.length; i++) {
                if (i > 0) jsonl += ",";
                String value = fields[i] == null ? "null" : "\"" + fields[i] + "\"";
                jsonl += "\"" + headers[i] + "\": " + value;
            }
            jsonl += "}";
            jsonlDocs.add(jsonl);
        }

        return jsonlDocs;
    }

    public enum ConvertableType {
        jsonl(""),
        tsv("\t"),
        csv(",");

        private final String separator;

        ConvertableType(String theSeparator) {
            separator = theSeparator;
        }
    }
}
