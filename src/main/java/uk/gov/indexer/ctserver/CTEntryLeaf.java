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
        byte[] decodeBytes = Base64.getDecoder().decode(leafHash);

        int decodeBytesLength = decodeBytes.length;

        //first byte is version
        version = decodeBytes[0];

        //second byte is leaftype
        merkleLeafType = decodeBytes[1];

        //next 8 bytes are timestamp
        timetamp = Arrays.copyOfRange(decodeBytes, 2, 10);

        //bytes at position 10 and 11 are entryType
        entryType = new byte[]{decodeBytes[10], decodeBytes[11]};

        //bytes at position 12, 13 and 14 are contentLength
        contentLength = new byte[]{decodeBytes[12], decodeBytes[13], decodeBytes[14]};

        //last two bytes in array are sctExtension so from byte 15 till (bytes array length -2) is payload
        payload = Arrays.copyOfRange(decodeBytes, 15, decodeBytesLength - 2);

        sctExtension = new byte[]{decodeBytes[decodeBytesLength - 1], decodeBytes[decodeBytesLength - 2]};
    }
}
