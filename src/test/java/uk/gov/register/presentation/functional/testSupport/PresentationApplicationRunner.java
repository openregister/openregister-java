package uk.gov.register.presentation.functional.testSupport;

import io.dropwizard.testing.ResourceHelpers;
import uk.gov.register.presentation.app.PresentationApplication;

public class PresentationApplicationRunner {
    public static final String DATABASE_URL = "jdbc:postgresql://localhost:5432/ft_presentation";

    public static void main(String[] args) throws Throwable {
        System.setProperty("dw.database.url", DATABASE_URL);

        String arg1= ResourceHelpers.resourceFilePath("test-app-config.yaml");
        String[] a = {"server", arg1};

        PresentationApplication.main(a);
        Thread.currentThread().join();

    }
}
