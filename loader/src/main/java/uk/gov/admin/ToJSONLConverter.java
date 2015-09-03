package uk.gov.admin;

import com.univocity.parsers.common.processor.RowListProcessor;
import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;
import com.univocity.parsers.tsv.TsvParser;
import com.univocity.parsers.tsv.TsvParserSettings;

import java.util.List;
import java.util.stream.Collectors;

public class ToJSONLConverter {
    private final static ToJSONLConverter identity =
            new ToJSONLConverter("jsonl") {
                @Override
                public List<String> convert(DataReader reader) {
                    return reader.reader().lines().collect(Collectors.toList());
                }
            };
    private final String type;

    private ToJSONLConverter(String type) {
        this.type = type;
    }

    public static ToJSONLConverter converterFor(String type) {
        if ("jsonl".equals(type)) return identity;

        return new ToJSONLConverter(type);
    }

    public List<String> convert(DataReader reader) {
        RowListProcessor rowProcessor;
        if ("tsv".equals(type))
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
        return rows.stream().map(fields -> {
            String jsonl = "{";
            for (int i = 0; i < headers.length; i++) {
                if (i > 0) jsonl += ", ";
                String value = fields[i] == null ? "\"\"" : "\"" + fields[i] + "\"";
                jsonl += "\"" + headers[i] + "\": " + value;
            }
            jsonl += "}";
            return jsonl;
        }).collect(Collectors.toList());


    }

    public enum ConvertibleType {
        jsonl, tsv, csv
    }
}
