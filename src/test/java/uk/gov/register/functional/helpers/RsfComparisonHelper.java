package uk.gov.register.functional.helpers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

public class RsfComparisonHelper {
	public static void assertRsfEqual(String rsf1, String rsf2) {
		if (rsf1.equals(rsf2)) {
			return;
		}

		List<String> rsf1Lines = Arrays.asList(rsf1.split("\n"));
		List<String> rsf2Lines = Arrays.asList(rsf2.split("\n"));

		assertThat("Different number of lines", rsf1Lines.size(), equalTo(rsf2Lines.size()));

		List<String> rsf1AddItemCommands = new ArrayList<>();
		List<String> rsf2AddItemCommands = new ArrayList<>();

		List<String> rsf1AppendEntryCommands = new ArrayList<>();
		List<String> rsf2AppendEntryCommands = new ArrayList<>();
		
		parseRsfCommands(rsf1Lines, rsf1AddItemCommands, rsf1AppendEntryCommands);
		parseRsfCommands(rsf2Lines, rsf2AddItemCommands, rsf2AppendEntryCommands);
		
		assertThat("Number of items not equal", rsf1AddItemCommands.size(), equalTo(rsf2AddItemCommands.size()));
		assertThat("Number of entries not equal", rsf1AppendEntryCommands.size(), equalTo(rsf2AppendEntryCommands.size()));
		
		// Check that all items are the same, regardless of order
		assertThat("Items not equal", rsf1AddItemCommands.containsAll(rsf2AddItemCommands), is(true));
		
		int numberOfEntries = rsf1AppendEntryCommands.size();

		// Check that entries are in the same order
		for (int i = 0; i < numberOfEntries; i++) {
			assertThat(String.format("Entry %s is not equal", i + 1), rsf1AppendEntryCommands.get(i), equalTo(rsf2AppendEntryCommands.get(i)));
		}
	}
	
	private static void parseRsfCommands(List<String> rsfLines, List<String> addItemCommands, List<String> appendEntryCommands) {
		rsfLines.stream().forEach(command -> {
			if (command.startsWith("add-item")) {
				addItemCommands.add(command);
			}
			else if (command.startsWith("append-entry")) {
				appendEntryCommands.add(command);
			}
		});
	}
}
