package uk.gov.register.core;

public abstract class Index {
	private final String indexName;
	private final String itemHash;

	public Index(String indexName, String itemHash) {
		this.indexName = indexName;
		this.itemHash = itemHash;
	}

	public String getIndexName() {
		return indexName;
	}

	public String getItemHash() {
		return itemHash;
	}
}