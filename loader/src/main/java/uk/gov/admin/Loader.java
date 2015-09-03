package uk.gov.admin;

import com.google.common.collect.Lists;
import com.jcabi.http.Request;
import com.jcabi.http.Response;
import com.jcabi.http.request.JdkRequest;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Loader {
    private static final int BATCH_SIZE = 2000;

    public static void main(String[] args) throws Exception {
        Map<String, String> argsMap = createArgumentsMap(args);

        String datafile = argsMap.get("--datafile");
        String mintUrl = argsMap.get("--mintUrl");
        String type = argsMap.get("--type");

        load(
                new DataReader(datafile),
                ToJSONLConverter.converterFor(type),
                mintUrl
        );

    }

    private static Exception printUsageException() throws Exception {
        return new RuntimeException("Usage: java Loader --mintUrl=<mint-load-url> --datafile=<loadfile.json> --type=<jsonl|tsv|csv>");
    }

    private static Map<String, String> createArgumentsMap(String[] args) throws Exception {
        try {

            Map<String, String> argsMap = Arrays.asList(args)
                    .stream()
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

    //Note: this might fail when file has large dataset
    public static void load(
            DataReader dataReader,
            ToJSONLConverter toJSONLConverter,
            String mintUrl) throws IOException {

        List<List<String>> batches = Lists.partition(toJSONLConverter.convert(dataReader), BATCH_SIZE);
        int totalEntriesLoaded = 0;
        for (List<String> batch : batches) {
            send(batch, mintUrl);
            System.out.println("Loaded " + (totalEntriesLoaded += batch.size()) + " entries...");
        }

    }

    private static void send(List<String> payload, String mintUrl) throws IOException {

        Response response = new JdkRequest(mintUrl)
                .method(Request.POST)
                .body()
                .set(String.join("\n", payload))
                .back()
                .fetch();
        if (response.status() != 200)
            throw new RuntimeException("Exception while loading entries: statusCode -> " + response.status() + " \n entity -> " + response.body());

    }
}
