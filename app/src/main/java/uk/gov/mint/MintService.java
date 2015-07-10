package uk.gov.mint;

import static spark.Spark.post;

public class MintService {
    private final LoadHandler loadHandler;

    public MintService(LoadHandler loadHandler) {
        this.loadHandler = loadHandler;
    }

    public void init() {
        post("/load", (req, res) -> {
            try {
                final String payload = req.body();
                loadHandler.handle(payload);
                return "OK";
            } catch (Exception e) {
                return "Error: " + e.getMessage();
            }
        });
    }

    public void shutdown() throws Exception {
        loadHandler.shutdown();
    }
}
