package uk.gov.register.protocols.s3;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.S3Object;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.net.MalformedURLException;
import java.net.URL;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class S3URLConnectionTest {

    @Mock
    private AmazonS3 s3client;
    @Mock
    private S3Object s3Object;

    @Before
    public void setProtocolHandlers(){
        System.setProperty("java.protocol.handler.pkgs", "uk.gov.register.protocols");
    }

    @Test
    public void shouldGetInputStream() throws Exception {
        when( s3client.getObject("bucket","key")).thenReturn(s3Object);

        URL url = new URL("s3://bucket/key");
        S3URLConnection connection = new S3URLConnection(url, s3client);
        connection.getInputStream();

        verify(s3Object).getObjectContent();
    }

    @Test(expected = MalformedURLException.class)
    public void shouldFailForInvalidScheme() throws Exception{
        URL url = new URL("zz://bucket/key");
        S3URLConnection connection = new S3URLConnection(url, s3client);
    }

    @Test(expected = MalformedURLException.class)
    public void shouldFailForInvalidPattern() throws Exception{
        URL url = new URL("s3://bucket");
        S3URLConnection connection = new S3URLConnection(url, s3client);
        connection.getInputStream();
    }

    @After
    public void unsetProtocolHandlers(){
        System.clearProperty("java.protocol.handler.pkgs");
    }
}