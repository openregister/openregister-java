package uk.gov.register.presentation;

import com.google.common.collect.ImmutableMap;
import org.hamcrest.collection.IsIterableContainingInOrder;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;

public class EntryViewTest {
    @Test
    public void getContent_returnsFieldsInSortedOrder() throws Exception {
        StringValue value = new StringValue("value");
        EntryView entryView = new EntryView(54, "theHash", "the-register",
                ImmutableMap.of(
                        "aaaa", value,
                        "xxxx", value,
                        "the-register", value,
                        "oooo", value,
                        "cccc", value));

        Iterable<String> fieldsInOrder = entryView.getContent().keySet();

        assertThat(fieldsInOrder, IsIterableContainingInOrder.contains("aaaa", "cccc", "oooo", "the-register", "xxxx"));
    }
}
