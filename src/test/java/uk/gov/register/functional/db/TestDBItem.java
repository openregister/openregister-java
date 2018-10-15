package uk.gov.register.functional.db;

import com.fasterxml.jackson.databind.JsonNode;
import uk.gov.register.core.Blob;
import uk.gov.register.util.HashValue;

public class TestDBItem {
    public final HashValue hashValue;
    public final JsonNode contents;

    public TestDBItem(HashValue hashValue, JsonNode contents) {
        this.hashValue = hashValue;
        this.contents = contents;
    }

    public TestDBItem(JsonNode contents) {
        this.hashValue = Blob.itemHash(contents);
        this.contents = contents;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TestDBItem that = (TestDBItem) o;

        if (hashValue != null ? !hashValue.equals(that.hashValue) : that.hashValue != null) return false;
        return contents.equals(that.contents);

    }

    @Override
    public int hashCode() {
        int result = hashValue != null ? hashValue.hashCode() : 0;
        result = 31 * result + contents.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "TestDBItem{" +
                "hashValue='" + hashValue.toString() + '\'' +
                ", contents=" + contents.toString() +
                '}';
    }
}
