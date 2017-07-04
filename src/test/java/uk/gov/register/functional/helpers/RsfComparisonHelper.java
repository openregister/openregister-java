package uk.gov.register.functional.helpers;

import uk.gov.register.serialization.RSFFormatter;
import uk.gov.register.serialization.RegisterCommand;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

public class RsfComparisonHelper {
	private static RSFFormatter rsfFormatter = new RSFFormatter();

	public static void assertRsfEqual(String rsf1, String rsf2) {
		if (rsf1.equals(rsf2)) {
			return;
		}

		String[] rsf1Lines = rsf1.split("\n");
		String[] rsf2Lines = rsf2.split("\n");

		assertThat(rsf1Lines.length, equalTo(rsf2Lines.length));

		for (int i = 0; i < rsf1Lines.length; i++) {
			// If adjacent rsf lines are both add-item, skip to next line
			if (rsf1Lines[i].startsWith("add-item") && rsf2Lines[i].startsWith("add-item")) {
				continue;
			}

			// Check that if entry has multiple hashes, the hashes are equal
			// (but not necessarily in the same order)
			if (rsf1Lines[i].startsWith("append-entry") && rsf2Lines[i].startsWith("append-entry")) {
				List<String> entry1Hashes = getItemHashesFromAppendEntryCommand(rsf1Lines[i]);
				List<String> entry2Hashes = getItemHashesFromAppendEntryCommand(rsf2Lines[i]);
				
				assertThat(entry1Hashes.size(), equalTo(entry2Hashes.size()));
				assertThat(entry1Hashes.containsAll(entry2Hashes), is(true));
				
				continue;
			}

			assertThat(rsf1Lines[i], equalTo(rsf2Lines[i]));
		}
	}
	
	private static List<String> getItemHashesFromAppendEntryCommand(String rsf) {
		RegisterCommand command = rsfFormatter.parse(rsf);
		return Arrays.asList(command.getCommandArguments().get(RSFFormatter.RSF_HASH_POSITION).split(";"));
	}
}
