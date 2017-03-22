package uk.gov.register.indexer;

import org.junit.Test;
import uk.gov.register.core.HashingAlgorithm;
import uk.gov.register.util.HashValue;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.core.IsEqual.equalTo;

public class IndexValueItemPairTest {
    @Test
    public void getValue_returnsValue() {
        IndexValueItemPair pair = new IndexValueItemPair("key", new HashValue(HashingAlgorithm.SHA256, "hash"));
        assertThat(pair.getValue(), equalTo("key"));
    }

    @Test
    public void getItemHash_returnsItemsHash() {
        IndexValueItemPair pair = new IndexValueItemPair("key", new HashValue(HashingAlgorithm.SHA256, "hash"));
        assertThat(pair.getItemHash(), equalTo(new HashValue(HashingAlgorithm.SHA256, "hash")));
    }

    @Test
    public void equals_shouldReturnFalse_whenValueIsNotEqual() {
        IndexValueItemPair pair1 = new IndexValueItemPair("value", new HashValue(HashingAlgorithm.SHA256, "hash"));
        IndexValueItemPair pair2 = new IndexValueItemPair("anotherValue", new HashValue(HashingAlgorithm.SHA256, "hash"));
        assertThat(pair1.equals(pair2), is(false));
    }

    @Test
    public void equals_shouldReturnFalse_whenItemHashIsNotEqual() {
        IndexValueItemPair pair1 = new IndexValueItemPair("value", new HashValue(HashingAlgorithm.SHA256, "hash1"));
        IndexValueItemPair pair2 = new IndexValueItemPair("value", new HashValue(HashingAlgorithm.SHA256, "hash2"));
        assertThat(pair1.equals(pair2), is(false));
    }

    @Test
    public void equals_shouldReturnTrue_whenPairsAreEqual() {
        IndexValueItemPair pair1 = new IndexValueItemPair("value", new HashValue(HashingAlgorithm.SHA256, "hash"));
        IndexValueItemPair pair2 = new IndexValueItemPair("value", new HashValue(HashingAlgorithm.SHA256, "hash"));
        assertThat(pair1.equals(pair2), is(true));
    }

    @Test
    public void hashcode_shouldReturnDifferentHashCode_whenValueIsNotEqual() {
        IndexValueItemPair pair1 = new IndexValueItemPair("value", new HashValue(HashingAlgorithm.SHA256, "hash"));
        IndexValueItemPair pair2 = new IndexValueItemPair("anotherValue", new HashValue(HashingAlgorithm.SHA256, "hash"));
        assertThat(pair1.hashCode(), not(pair2.hashCode()));
    }

    @Test
    public void hashcode_shouldReturnDifferentHashCode_whenItemHashIsNotEqual() {
        IndexValueItemPair pair1 = new IndexValueItemPair("value", new HashValue(HashingAlgorithm.SHA256, "hash1"));
        IndexValueItemPair pair2 = new IndexValueItemPair("value", new HashValue(HashingAlgorithm.SHA256, "hash2"));
        assertThat(pair1.hashCode(), not(pair2.hashCode()));
    }

    @Test
    public void hashcode_shouldReturnSameHashcode_whenPairsAreEqual() {
        IndexValueItemPair pair1 = new IndexValueItemPair("value", new HashValue(HashingAlgorithm.SHA256, "hash"));
        IndexValueItemPair pair2 = new IndexValueItemPair("value", new HashValue(HashingAlgorithm.SHA256, "hash"));
        assertThat(pair1.hashCode(), is(pair2.hashCode()));
    }
}
