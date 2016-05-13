package uk.gov.mint;


public class Entry {
    private final int entry_number;
    private final String sha256hex;

    public Entry(int entry_number, String sha256hex) {
        this.entry_number = entry_number;
        this.sha256hex = sha256hex;
    }

    @SuppressWarnings("unused, used from DAO")
    public int getEntry_number() {
        return entry_number;
    }

    @SuppressWarnings("unused, used from DAO")
    public String getSha256hex() {
        return sha256hex;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Entry entry = (Entry) o;

        if (entry_number != entry.entry_number) return false;
        return sha256hex.equals(entry.sha256hex);

    }

    @Override
    public int hashCode() {
        int result = entry_number;
        result = 31 * result + sha256hex.hashCode();
        return result;
    }
}
