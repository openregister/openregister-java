package uk.gov.admin;

import joptsimple.OptionParser;
import joptsimple.OptionSet;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LoaderArgsParser {
    private static final String usageMessage =
            "Usage: java Loader [--overwrite] --configfile=<config.properties> [--schemafile=<dataschema.json>] --datafile=<loadfile.json>";
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
        parser.accepts("schemafile", "File containing the schema that describes the data format.").withRequiredArg();
        parser.accepts("datafile", "File containing data to load. Currently only JSON is accepted.").withRequiredArg().required();
        parser.accepts("configfile", "File containing configuration in regular java.util.Properties format.").withRequiredArg().required();
        parser.accepts("overwrite", "Overwrite existing data.").withOptionalArg();

        return parser;
    }

    private LoaderArgs optionsToLoaderArgs(OptionSet options) {
        final String datafile = (String) options.valueOf("datafile");
        final String configfile = (String) options.valueOf("configfile");
        final Boolean overwrite = options.has("overwrite");

        reader = new DataReader(datafile);

        Map<String, Object> config;
        try (final FileInputStream configInStream = new FileInputStream(configfile)) {
            final Properties configProps = new Properties();
            configProps.load(configInStream);

            config = configProps.entrySet().stream()
                    .collect(Collectors.toMap(e -> (String) e.getKey(), Map.Entry::getValue));

            return new LoaderArgs(reader.streamData(), config, overwrite);
        } catch (IOException e) {
            throw new RuntimeException("Error occurred loading configfile: " + configfile, e);
        }
    }

    public class LoaderArgs {
        public final Stream<String> data;
        public final Map<String, Object> config;
        public final boolean overwrite;

        public LoaderArgs(Stream<String> data, Map<String, Object> config, boolean overwrite) {
            this.data = data;
            this.config = config;
            this.overwrite = overwrite;
        }
    }
}
