package uk.gov.admin;

import joptsimple.OptionParser;
import joptsimple.OptionSet;

public class LoaderArgsParser {
    private static final String usageMessage =
            "Usage: java Loader --mintUrl=<mint-load-url> --datafile=<loadfile.json> --type=<jsonl|tsv|csv>";
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
        parser.accepts("mintUrl", "The mint url for loading data.").withRequiredArg().required();
        parser.accepts("type", "Type of data file: JSONL, tsv or csv. Defaults to JSONL").withRequiredArg();

        return parser;
    }

    private LoaderArgs optionsToLoaderArgs(OptionSet options) {
        final String datafile = (String) options.valueOf("datafile");
        final String mintUrl = (String) options.valueOf("mintUrl");
        final String type = options.valueOf("type") == null ? "jsonl" : (String) options.valueOf("type");

        reader = new DataReader(datafile);

        return new LoaderArgs(reader, mintUrl, type);
    }

    public class LoaderArgs {
        public final DataReader dataReader;
        public final String mintUrl;
        public final String type;

        public LoaderArgs(DataReader dataReader, String mintUrl, String type) {
            this.dataReader = dataReader;
            this.mintUrl = mintUrl;
            this.type = type;
        }
    }
}
