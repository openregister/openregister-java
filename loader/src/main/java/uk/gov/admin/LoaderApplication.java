package uk.gov.admin;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LoaderApplication {
    public static void main(String[] args) throws Exception {
        Map<String, String> argsMap = createArgumentsMap(args);

        String dataFile = argsMap.get("--datafile");
        String mintUrl = argsMap.get("--mintUrl");
        String type = argsMap.get("--type");


        DataFileReader dataFileReader = new DataFileReader(dataFile, type);

        new Loader(mintUrl).load(dataFileReader.getFileEntriesIterator());

    }

    private static Map<String, String> createArgumentsMap(String[] args) throws Exception {
        try {

            Map<String, String> argsMap = Stream.of(args)
                    .map(a -> a.split("="))
                    .collect(Collectors.toMap(argEntry -> argEntry[0], argEntry -> argEntry[1]));

            if (argsMap.containsKey("--datafile") && argsMap.containsKey("--mintUrl") && argsMap.containsKey("--type")) {
                return argsMap;
            }

            throw printUsageException();

        } catch (ArrayIndexOutOfBoundsException e) {

            throw printUsageException();

        }
    }

    private static Exception printUsageException() throws Exception {
        return new RuntimeException("Usage: java LoaderApplication --mintUrl=<mint-load-url> --datafile=<loadfile.json> --type=<jsonl|tsv|csv>");
    }

}
