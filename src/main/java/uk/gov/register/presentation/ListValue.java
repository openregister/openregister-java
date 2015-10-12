package uk.gov.register.presentation;

import com.google.common.collect.ImmutableList;

import java.util.Iterator;
import java.util.List;

public class ListValue implements FieldValue, Iterable<FieldValue> {
    private List<FieldValue> elements;

    public ListValue(Iterable<FieldValue> elements) {
        this.elements = ImmutableList.copyOf(elements);
    }

    @Override
    public String value() {
        return null;
    }

    @Override
    public Iterator<FieldValue> iterator() {
        return elements.iterator();
    }
}
