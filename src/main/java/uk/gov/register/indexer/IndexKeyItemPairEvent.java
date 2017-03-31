package uk.gov.register.indexer;

import uk.gov.register.util.HashValue;

public class IndexKeyItemPairEvent {
    private final IndexKeyItemPair pair;
    private final boolean isStart;

    public IndexKeyItemPairEvent(IndexKeyItemPair pair, boolean isStart) {
        this.pair = pair;
        this.isStart = isStart;
    }

    public HashValue getItemHash() {
        return pair.getItemHash();
    }

    public String getIndexKey() {
        return pair.getIndexKey();
    }

    public boolean isStart() {
        return isStart;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        IndexKeyItemPairEvent that = (IndexKeyItemPairEvent) o;

        if (isStart != that.isStart) return false;
        return pair.equals(that.pair);

    }

    @Override
    public int hashCode() {
        int result = pair.hashCode();
        result = 31 * result + (isStart ? 1 : 0);
        return result;
    }
}
