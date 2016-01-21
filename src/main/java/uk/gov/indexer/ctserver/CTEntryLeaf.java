package uk.gov.indexer.ctserver;

import java.util.Arrays;
import java.util.Base64;

@SuppressWarnings({"FieldCanBeLocal", "unused"})
public class CTEntryLeaf {
    public final byte version;

    public final byte merkleLeafType;

    public final byte[] timetamp;

    public final byte[] entryType;

    public final byte[] contentLength;

    public final byte[] payload;

    public final byte[] sctExtension;

    public CTEntryLeaf(String leafHash) {
        byte[] decodedBytes = Base64.getDecoder().decode(leafHash);

        int decodeBytesLength = decodedBytes.length;

        //first byte is version
        version = decodedBytes[0];

        //second byte is leaftype
        merkleLeafType = decodedBytes[1];

        //next 8 bytes are timestamp
        timetamp = Arrays.copyOfRange(decodedBytes, 2, 10);

        //bytes at position 10 and 11 are entryType
        entryType = new byte[]{decodedBytes[10], decodedBytes[11]};

        //bytes at position 12, 13 and 14 are contentLength
        contentLength = new byte[]{decodedBytes[12], decodedBytes[13], decodedBytes[14]};

        //last two bytes in array are sctExtension so from byte 15 till (bytes array length -2) is payload
        payload = Arrays.copyOfRange(decodedBytes, 15, decodeBytesLength - 2);

        sctExtension = new byte[]{decodedBytes[decodeBytesLength - 2], decodedBytes[decodeBytesLength - 1]};
    }
}
