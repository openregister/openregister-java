package uk.gov.admin;

import com.univocity.parsers.common.processor.RowListProcessor;
import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;
import com.univocity.parsers.tsv.TsvParser;
import com.univocity.parsers.tsv.TsvParserSettings;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ToJSONLConverter {
    private final static ToJSONLConverter identity =
            new ToJSONLConverter(ConvertibleType.jsonl) {
                @Override
                public List<String> convert(DataReader reader) {
                    return reader.streamData().collect(Collectors.toList());
                }
            };
    private final ConvertibleType convertibleType;

    private ToJSONLConverter(ConvertibleType theConvertibleType) {
        this.convertibleType = theConvertibleType;
    }

    public static ToJSONLConverter converterFor(ConvertibleType convertibleType) {
        if (convertibleType == ConvertibleType.jsonl) return identity;

        return new ToJSONLConverter(convertibleType);
    }

    public List<String> convert(DataReader reader) {
        RowListProcessor rowProcessor;
        if (convertibleType == ConvertibleType.tsv)
            rowProcessor = getTsvParser(reader);
        else
            rowProcessor = getCsvParser(reader);

        String[] headers = rowProcessor.getHeaders();
        List<String[]> rows = rowProcessor.getRows();

        return convertRecordsToJsonl(headers, rows);
    }

    private RowListProcessor getCsvParser(DataReader reader) {
        CsvParserSettings parserSettings = new CsvParserSettings();
        parserSettings.setLineSeparatorDetectionEnabled(true);
        RowListProcessor rowProcessor = new RowListProcessor();
        parserSettings.setRowProcessor(rowProcessor);
        parserSettings.setHeaderExtractionEnabled(true);
        CsvParser parser = new CsvParser(parserSettings);
        parser.parse(reader.reader());

        return rowProcessor;
    }

    private RowListProcessor getTsvParser(DataReader reader) {
        TsvParserSettings parserSettings = new TsvParserSettings();
        parserSettings.setLineSeparatorDetectionEnabled(true);
        RowListProcessor rowProcessor = new RowListProcessor();
        parserSettings.setRowProcessor(rowProcessor);
        parserSettings.setHeaderExtractionEnabled(true);
        TsvParser parser = new TsvParser(parserSettings);
        parser.parse(reader.reader());

        return rowProcessor;
    }

    // Build string representation of json - faster than using Json parser.
    private List<String> convertRecordsToJsonl(String[] headers, List<String[]> rows) {
        List<String> jsonlDocs = new ArrayList<>();
        for (String[] fields : rows) {
            String jsonl = "{";
            for (int i = 0; i < headers.length; i++) {
                if (i > 0) jsonl += ", ";
                String value = fields[i] == null ? "\"\"" : "\"" + fields[i] + "\"";
                jsonl += "\"" + headers[i] + "\": " + value;
            }
            jsonl += "}";
            jsonlDocs.add(jsonl);
        }

        return jsonlDocs;
    }

    public enum ConvertibleType {
        jsonl(""),
        tsv("\t"),
        csv(",");

        private final String separator;

        ConvertibleType(String theSeparator) {
            separator = theSeparator;
        }
    }
}
