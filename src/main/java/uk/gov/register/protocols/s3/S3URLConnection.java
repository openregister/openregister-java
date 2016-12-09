package uk.gov.register.protocols.s3;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.S3Object;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class S3URLConnection extends URLConnection {

    private final Pattern s3Pattern = Pattern.compile("s3://(.+)/(.+)");

    private final AmazonS3 s3Client;

    S3URLConnection(URL url) {
        super(url);
        s3Client = new AmazonS3Client();
    }

    S3URLConnection(URL url, AmazonS3 s3Client) {
        super(url);
        this.s3Client = s3Client;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        Map<String, String> urlComponents = extractUrlComponents();
        final S3Object object = s3Client.getObject(urlComponents.get("bucket"), urlComponents.get("key"));
        return object.getObjectContent();
    }

    @Override
    public void connect() throws IOException {
        // do nothing
    }

    private Map<String, String> extractUrlComponents() throws MalformedURLException {
        Map<String, String> urlComponents = new HashMap<>();
        Matcher matcher = s3Pattern.matcher(url.toString());
        if (matcher.matches() && matcher.groupCount() == 2) {
            urlComponents.put("bucket", matcher.group(1));
            urlComponents.put("key", matcher.group(2));
            return urlComponents;
        }
        throw new MalformedURLException("url did not match pattern for s3 url: " + url.toString());
    }


}
