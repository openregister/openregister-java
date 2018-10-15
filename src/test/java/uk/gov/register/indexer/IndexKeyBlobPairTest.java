package uk.gov.register.indexer;

import org.junit.Test;
import uk.gov.register.core.HashingAlgorithm;
import uk.gov.register.util.HashValue;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.core.IsEqual.equalTo;

public class IndexKeyBlobPairTest {
    @Test
    public void getValue_returnsValue() {
        IndexKeyItemPair pair = new IndexKeyItemPair("key", new HashValue(HashingAlgorithm.SHA256, "hash"));
        assertThat(pair.getIndexKey(), equalTo("key"));
    }

    @Test
    public void getItemHash_returnsItemsHash() {
        IndexKeyItemPair pair = new IndexKeyItemPair("key", new HashValue(HashingAlgorithm.SHA256, "hash"));
        assertThat(pair.getItemHash(), equalTo(new HashValue(HashingAlgorithm.SHA256, "hash")));
    }

    @Test
    public void equals_shouldReturnFalse_whenValueIsNotEqual() {
        IndexKeyItemPair pair1 = new IndexKeyItemPair("value", new HashValue(HashingAlgorithm.SHA256, "hash"));
        IndexKeyItemPair pair2 = new IndexKeyItemPair("anotherValue", new HashValue(HashingAlgorithm.SHA256, "hash"));
        assertThat(pair1.equals(pair2), is(false));
    }

    @Test
    public void equals_shouldReturnFalse_whenItemHashIsNotEqual() {
        IndexKeyItemPair pair1 = new IndexKeyItemPair("value", new HashValue(HashingAlgorithm.SHA256, "hash1"));
        IndexKeyItemPair pair2 = new IndexKeyItemPair("value", new HashValue(HashingAlgorithm.SHA256, "hash2"));
        assertThat(pair1.equals(pair2), is(false));
    }

    @Test
    public void equals_shouldReturnTrue_whenPairsAreEqual() {
        IndexKeyItemPair pair1 = new IndexKeyItemPair("value", new HashValue(HashingAlgorithm.SHA256, "hash"));
        IndexKeyItemPair pair2 = new IndexKeyItemPair("value", new HashValue(HashingAlgorithm.SHA256, "hash"));
        assertThat(pair1.equals(pair2), is(true));
    }

    @Test
    public void hashcode_shouldReturnDifferentHashCode_whenValueIsNotEqual() {
        IndexKeyItemPair pair1 = new IndexKeyItemPair("value", new HashValue(HashingAlgorithm.SHA256, "hash"));
        IndexKeyItemPair pair2 = new IndexKeyItemPair("anotherValue", new HashValue(HashingAlgorithm.SHA256, "hash"));
        assertThat(pair1.hashCode(), not(pair2.hashCode()));
    }

    @Test
    public void hashcode_shouldReturnDifferentHashCode_whenItemHashIsNotEqual() {
        IndexKeyItemPair pair1 = new IndexKeyItemPair("value", new HashValue(HashingAlgorithm.SHA256, "hash1"));
        IndexKeyItemPair pair2 = new IndexKeyItemPair("value", new HashValue(HashingAlgorithm.SHA256, "hash2"));
        assertThat(pair1.hashCode(), not(pair2.hashCode()));
    }

    @Test
    public void hashcode_shouldReturnSameHashcode_whenPairsAreEqual() {
        IndexKeyItemPair pair1 = new IndexKeyItemPair("value", new HashValue(HashingAlgorithm.SHA256, "hash"));
        IndexKeyItemPair pair2 = new IndexKeyItemPair("value", new HashValue(HashingAlgorithm.SHA256, "hash"));
        assertThat(pair1.hashCode(), is(pair2.hashCode()));
    }
}
