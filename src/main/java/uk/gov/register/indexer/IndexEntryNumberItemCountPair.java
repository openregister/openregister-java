package uk.gov.register.indexer;

import java.util.Optional;

public class IndexEntryNumberItemCountPair {
	private final int existingItemCount;
	private final Optional<Integer> startIndexEntryNumber;

	public IndexEntryNumberItemCountPair(Optional<Integer> startIndexEntryNumber, int existingItemCount) {
		this.startIndexEntryNumber = startIndexEntryNumber;
		this.existingItemCount = existingItemCount;
	}

	public int getExistingItemCount() {
		return existingItemCount;
	}
	
	public Optional<Integer> getStartIndexEntryNumber() {
		return startIndexEntryNumber;
	}
}
