package uk.gov.functional.db;

import com.fasterxml.jackson.databind.JsonNode;
import uk.gov.mint.Item;

public class TestDBItem {
    public final String sha256hex;
    public final JsonNode contents;

    public TestDBItem(String sha256hex, JsonNode contents) {
        this.sha256hex = sha256hex;
        this.contents = contents;
    }

    public TestDBItem(JsonNode contents) {
        this.sha256hex = Item.itemHash(contents);
        this.contents = contents;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TestDBItem that = (TestDBItem) o;

        if (sha256hex != null ? !sha256hex.equals(that.sha256hex) : that.sha256hex != null) return false;
        return contents.equals(that.contents);

    }

    @Override
    public int hashCode() {
        int result = sha256hex != null ? sha256hex.hashCode() : 0;
        result = 31 * result + contents.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "TestDBItem{" +
                "sha256hex='" + sha256hex + '\'' +
                ", contents=" + contents.toString() +
                '}';
    }
}
