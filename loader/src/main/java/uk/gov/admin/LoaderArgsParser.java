package uk.gov.admin;

import joptsimple.OptionParser;
import joptsimple.OptionSet;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

public class LoaderArgsParser {
    private static final String usageMessage =
            "Usage: java Loader --configfile=<config.properties> --datafile=<loadfile.json> --type=<jsonl|tsv|csv>";
    private DataReader reader;

    LoaderArgs parseArgs(String[] args) {

        final OptionSet options;
        try {
            options = optionParser().parse(args);

            return optionsToLoaderArgs(options);
        } catch (Exception e) {
            throw new RuntimeException(usageMessage, e);
        }
    }

    private OptionParser optionParser() {
        OptionParser parser = new OptionParser();
        parser.accepts("datafile", "File containing data to load. Currently only JSON is accepted.").withRequiredArg().required();
        parser.accepts("configfile", "File containing configuration in regular java.util.Properties format.").withRequiredArg().required();
        parser.accepts("type", "Type of data file: JSONL, tsv or csv. Defaults to JSONL").withRequiredArg();

        return parser;
    }

    private LoaderArgs optionsToLoaderArgs(OptionSet options) {
        final String datafile = (String) options.valueOf("datafile");
        final String configfile = (String) options.valueOf("configfile");
        final String type = options.valueOf("type") == null ? "jsonl" : (String) options.valueOf("type");

        reader = new DataReader(datafile);

        Map<String, Object> config;
        try (final FileInputStream configInStream = new FileInputStream(configfile)) {
            final Properties configProps = new Properties();
            configProps.load(configInStream);

            config = configProps.entrySet().stream()
                    .collect(Collectors.toMap(e -> (String) e.getKey(), Map.Entry::getValue));

            return new LoaderArgs(reader, config, type);
        } catch (IOException e) {
            throw new RuntimeException("Error occurred loading configfile: " + configfile, e);
        }
    }

    public class LoaderArgs {
        public final DataReader dataReader;
        public final Map<String, Object> config;
        public final String type;

        public LoaderArgs(DataReader dataReader, Map<String, Object> config, String type) {
            this.dataReader = dataReader;
            this.config = config;
            this.type = type;
        }
    }
}
