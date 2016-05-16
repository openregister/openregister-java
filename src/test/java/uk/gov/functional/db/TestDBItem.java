package uk.gov.functional.db;

import org.apache.commons.codec.digest.DigestUtils;

import java.util.Arrays;

public class TestDBItem {
    public final String sha256hex;
    public final byte[] contents;

    public TestDBItem(String sha256hex, byte[] contents) {
        this.sha256hex = sha256hex;
        this.contents = contents;
    }

    public TestDBItem(byte[] contents) {
        this.sha256hex = DigestUtils.sha256Hex(contents);
        this.contents = contents;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TestDBItem that = (TestDBItem) o;

        if (sha256hex != null ? !sha256hex.equals(that.sha256hex) : that.sha256hex != null) return false;
        return Arrays.equals(contents, that.contents);

    }

    @Override
    public int hashCode() {
        int result = sha256hex != null ? sha256hex.hashCode() : 0;
        result = 31 * result + Arrays.hashCode(contents);
        return result;
    }
}
