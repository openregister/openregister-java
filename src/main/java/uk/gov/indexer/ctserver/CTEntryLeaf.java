package uk.gov.indexer.ctserver;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Arrays;
import java.util.Base64;

@SuppressWarnings({"FieldCanBeLocal", "unused"})

@JsonIgnoreProperties({"extra_data"})
public class CTEntryLeaf {
    private final byte version;

    private final byte merkleLeafType;

    private final byte[] timetamp;

    private final byte[] entryType;

    private final byte[] contentLength;

    private final byte[] payload;

    private final byte[] sctExtension;

    private final String leafInput;

    @JsonCreator
    public CTEntryLeaf(@JsonProperty("leaf_input") String leafInput) {
        this.leafInput = leafInput;

        byte[] decodedBytes = Base64.getDecoder().decode(leafInput);

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

    public byte[] getPayload() {
        return payload;
    }

    public String getLeafInput() {
        return leafInput;
    }
}
