package uk.gov.admin;

import com.jcabi.http.Request;
import com.jcabi.http.Response;
import com.jcabi.http.request.JdkRequest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

class Loader {

    private static final int BATCH_SIZE = 2000;

    private final String mintUrl;
    private long entryCount = 0;

    public Loader(String mintUrl) {
        this.mintUrl = mintUrl;
    }

    public void load(Iterator<String> fileEntries) throws IOException {

        List<String> entryBatch = new ArrayList<>();

        while (fileEntries.hasNext()) {

            entryBatch.add(fileEntries.next());

            if (++entryCount % BATCH_SIZE == 0) {
                send(entryBatch);
                entryBatch = new ArrayList<>();
            }

        }

        if (!entryBatch.isEmpty()) {
            send(entryBatch);
        }
    }

    private void send(List<String> batch) throws IOException {

        Response response = new JdkRequest(mintUrl)
                .method(Request.POST)
                .body()
                .set(String.join("\n", batch))
                .back()
                .fetch();
        if (response.status() != 200) {
            throw new RuntimeException("Exception while loading entries: statusCode -> " + response.status() + "\n" +
                    " entity -> " + response.body());
        }

        System.out.println("Loaded " + entryCount + " entries...");
    }
}
