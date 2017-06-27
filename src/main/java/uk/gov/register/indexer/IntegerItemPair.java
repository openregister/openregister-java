package uk.gov.register.indexer;

import java.util.Optional;

public class IntegerItemPair {
	private final Optional<Integer> integer1;
	private final int integer2;

	public IntegerItemPair(Optional<Integer> integer1, int integer2) {
		this.integer1 = integer1;
		this.integer2 = integer2;
	}

	public Optional<Integer> getStartIndexEntryNumber() {
		return integer1;
	}

	public int getExistingItemCount() {
		return integer2;
	}
}
