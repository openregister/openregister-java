package uk.gov.register.protocols.classpath;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class ClasspathURLConnectionTest {
    @Before
    public void setProtocolHandlers(){
        System.setProperty("java.protocol.handler.pkgs", "uk.gov.register.protocols");
    }

    @Test
    public void shouldGetInputStream() throws Exception {
        URL url = new URL("classpath://config/external-fields.json");
        ClasspathURLConnection connection = new ClasspathURLConnection(url);
        InputStream inputStream = connection.getInputStream();
        Scanner scanner = new Scanner(inputStream);
        String line1 = scanner.nextLine();
        assertThat(line1, is("{"));
        String line2 = scanner.nextLine();
        assertThat(line2, is("  \"country\": {"));

    }

    @Test(expected = MalformedURLException.class)
    public void shouldFailForInvalidScheme() throws Exception{
        URL url = new URL("zz:///my-path");
        ClasspathURLConnection connection = new ClasspathURLConnection(url);
    }

    @After
    public void unsetProtocolHandlers(){
        System.clearProperty("java.protocol.handler.pkgs");
    }
}