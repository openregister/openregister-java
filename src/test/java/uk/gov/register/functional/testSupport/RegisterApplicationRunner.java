package uk.gov.register.functional.testSupport;

import io.dropwizard.testing.ResourceHelpers;
import uk.gov.register.RegisterApplication;

public class RegisterApplicationRunner {
    public static final String DATABASE_URL = "jdbc:postgresql://localhost:5432/ft_openregister_java";

    public static void main(String[] args) throws Throwable {
        System.setProperty("dw.database.url", DATABASE_URL);

        String arg1= ResourceHelpers.resourceFilePath("test-app-config.yaml");
        String[] a = {"server", arg1};

        RegisterApplication.main(a);
        Thread.currentThread().join();

    }
}
