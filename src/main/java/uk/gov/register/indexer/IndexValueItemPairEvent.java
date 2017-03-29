package uk.gov.register.indexer;

import uk.gov.register.util.HashValue;

public class IndexValueItemPairEvent {
    private final IndexValueItemPair pair;
    private final boolean isStart;

    public IndexValueItemPairEvent(IndexValueItemPair pair, boolean isStart) {
        this.pair = pair;
        this.isStart = isStart;
    }

    public HashValue getItemHash() {
        return pair.getItemHash();
    }

    public String getIndexValue() {
        return pair.getValue();
    }

    public boolean isStart() {
        return isStart;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        IndexValueItemPairEvent that = (IndexValueItemPairEvent) o;

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
