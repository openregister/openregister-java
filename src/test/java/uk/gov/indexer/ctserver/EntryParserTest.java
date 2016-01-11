package uk.gov.indexer.ctserver;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.indexer.dao.Entry;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;


@RunWith(MockitoJUnitRunner.class)
public class EntryParserTest {
    private final String validLeafData = "AAAAAAFSIJm7NoAAAACMeyAiYnVzaW5lc3MiOiAiY29tcGFueTowNzIyODEzMCIsICJlbmQtZGF0ZSI6ICIiLCAiZm9vZC1wcmVtaXNlcyI6ICI3NTkzMzIiLCAiZm9vZC1wcmVtaXNlcy10eXBlcyI6IFsgXSwgIm5hbWUiOiAiQnlyb24iLCAic3RhcnQtZGF0ZSI6ICIiIH0AAA==";

    @Test(expected = RuntimeException.class)
    public void invalidDataThrowsException() {
        MerkleTreeLeaf inputGarbage = new MerkleTreeLeaf();
        inputGarbage.setLeaf_input("non-JSON data");

        EntryParser objUnderTest = new EntryParser();
        objUnderTest.parse(inputGarbage, "signature", -1);
        fail("Should have thrown an exception");
    }

    @Test
    public void validDataReturnsAValidEntryObject() {
        MerkleTreeLeaf inputGarbage = new MerkleTreeLeaf();
        inputGarbage.setLeaf_input(validLeafData);

        EntryParser objUnderTest = new EntryParser();
        Entry e = objUnderTest.parse(inputGarbage, "signature", -1);

        assertThat(e.contents, notNullValue());
        assertThat(e.serial_number, equalTo(-1));
    }
}
