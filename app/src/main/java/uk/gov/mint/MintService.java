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
                System.out.println("received payload = " + payload);
                // Do something with this ...
                loadHandler.handle(payload);
                return "OK";
            } catch (Exception e) {
                return "Error: " + e.getMessage();
            }
        });
    }
}
