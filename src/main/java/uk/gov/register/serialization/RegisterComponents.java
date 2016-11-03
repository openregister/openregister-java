package uk.gov.register.serialization;

import uk.gov.register.core.Entry;
import uk.gov.register.core.Item;

import java.util.List;
import java.util.Set;

public class RegisterComponents {

    public final List<Entry> entries;

    public final Set<Item> items;

    public RegisterComponents(List<Entry> entries, Set<Item> items) {
        this.entries = entries;
        this.items = items;
    }

    @Override
    public String toString() {
        return "RegisterComponents{" +
                "entries=" + entries +
                ", items=" + items +
                '}';
    }
}
