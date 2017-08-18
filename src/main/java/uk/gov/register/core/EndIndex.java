package uk.gov.register.core;

public class EndIndex extends Index {
	private final String entryKey;
	private final String indexKey;
	private final int endEntryNumber;
	private final int endIndexEntryNumber;
	private final int entryNumberToEnd;

	public EndIndex(String indexName, String entryKey, String indexKey, String itemHash, int endEntryNumber, int endIndexEntryNumber, int entryNumberToEnd) {
		super(indexName, itemHash);
		this.entryKey = entryKey;
		this.indexKey = indexKey;
		this.endEntryNumber = endEntryNumber;
		this.endIndexEntryNumber = endIndexEntryNumber;
		this.entryNumberToEnd = entryNumberToEnd;
	}

	@SuppressWarnings("unused, used by @BindBean")
	public String getEntryKey() {
		return entryKey;
	}

	@SuppressWarnings("unused, used by @BindBean")
	public String getIndexKey() {
		return indexKey;
	}

	@SuppressWarnings("unused, used by @BindBean")
	public int getEndEntryNumber() {
		return endEntryNumber;
	}

	@SuppressWarnings("unused, used by @BindBean")
	public int getEndIndexEntryNumber() {
		return endIndexEntryNumber;
	}
	
	public int getEntryNumberToEnd() {
		return entryNumberToEnd;
	}
}