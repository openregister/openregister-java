package uk.gov.admin;

import com.jcabi.http.Request;
import com.jcabi.http.Response;
import com.jcabi.http.request.JdkRequest;
import javaslang.collection.List;
import uk.gov.admin.ToJSONLConverter.ConvertibleType;

public class Loader {
    private LoaderArgsParser.LoaderArgs loaderArgs;

    Loader(LoaderArgsParser.LoaderArgs loaderArgs) {
        this.loaderArgs = loaderArgs;
    }

    public static void main(String[] args) {
        final LoaderArgsParser.LoaderArgs loaderArgs = new LoaderArgsParser().parseArgs(args);

        new Loader(loaderArgs).load();
    }

    public void load() {
        try {
            final ToJSONLConverter converter = ToJSONLConverter.converterFor(ConvertibleType.valueOf(loaderArgs.type));
            converter.convert(loaderArgs.dataReader)
                    .grouped(1000)
                    .forEach(this::send);
        } catch (Throwable t) {
            throw new RuntimeException("Error occurred publishing datafile to queue", t);
        }
    }

    private void send(List<String> payload) {
        try {
            final String mintUrl = (String) loaderArgs.config.get("mintUrl");
            Response r = new JdkRequest(mintUrl)
                    .method(Request.POST)
                    .body()
                    .set(payload.join("\n"))
                    .back()
                    .fetch();
            if (r.status() != 200)
                System.err.println("Unexpected result: " + r.body());
        } catch (Exception e) {
            System.err.println("Error occurred sending data to mint: " + e);
        }
    }
}
