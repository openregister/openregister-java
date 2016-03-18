package uk.gov.mint;

import org.junit.Assert;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;

public class ItemTest {
    @Test
    public void twoItemsAreSameIfHashIsSame(){
        String json = "{\"key\":\"value\"}";
        Item item1 = new Item(json.getBytes());
        Item item2 = new Item(json.getBytes());
        Assert.assertThat(item1, equalTo(item2));
    }
}
