package uk.gov.register.core;

public class StartIndex extends Index {
	private final String key;
	private final int startEntryNumber;
	private final int startIndexEntryNumber;
	private boolean ended;

	public StartIndex(String indexName, String key, String itemHash, int startEntryNumber, int startIndexEntryNumber) {
		super(indexName, itemHash);
		this.key = key;
		this.startEntryNumber = startEntryNumber;
		this.startIndexEntryNumber = startIndexEntryNumber;
	}

	@SuppressWarnings("unused, used by @BindBean")
	public String getKey() {
		return key;
	}

	@SuppressWarnings("unused, used by @BindBean")
	public int getStartEntryNumber() {
		return startEntryNumber;
	}

	@SuppressWarnings("unused, used by @BindBean")
	public int getStartIndexEntryNumber() {
		return startIndexEntryNumber;
	}
	
	public boolean isEnded() {
		return ended;
	}
	
	public void end() {
		ended = true;
	}
}