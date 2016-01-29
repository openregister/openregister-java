package uk.gov.register.presentation.functional.testSupport;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class CTLeafInputGenerator {
    public static String createLeafInputFrom(String payloadJsonString, long timestamp) {
        try {
            byte[] payload = payloadJsonString.getBytes(StandardCharsets.UTF_8);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            //write version and merkleLeafType
            baos.write(new byte[]{0, 0});

            //writing timestamp
            ByteBuffer longBuffer = ByteBuffer.allocate(8);
            longBuffer.putLong(timestamp);
            baos.write(longBuffer.array());

            //writing entrytype
            baos.write(-128);
            baos.write(0);

            //write content length
            int contentLengthByteArraySize = 3;
            byte[] contentLength = new byte[]{0, 0, 0};
            byte[] src = BigInteger.valueOf(payload.length).toByteArray();
            for (int i = src.length - 1; i >= 0; i--) {
                contentLength[--contentLengthByteArraySize] = src[i];
            }

            baos.write(contentLength);

            //write payload
            baos.write(payloadJsonString.getBytes());

            //write sctExtension
            baos.write(new byte[]{0, 0});

            return Base64.getEncoder().encodeToString(baos.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
