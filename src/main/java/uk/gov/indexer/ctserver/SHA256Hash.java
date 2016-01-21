package uk.gov.indexer.ctserver;

import org.apache.commons.codec.binary.Hex;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class SHA256Hash {
    public static String createHash(byte[] payload) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(payload);
            return Hex.encodeHexString(md.digest());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
