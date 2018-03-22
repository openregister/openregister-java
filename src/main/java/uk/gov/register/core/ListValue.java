package uk.gov.register.core;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.ImmutableList;

import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

public class ListValue implements FieldValue, Iterable<FieldValue> {
    private List<FieldValue> elements;

    public ListValue(Iterable<FieldValue> elements) {
        this.elements = ImmutableList.copyOf(elements);
    }

    @Override
    public boolean isList() {
        return true;
    }

    @Override
    @JsonIgnore
    public String getValue() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterator<FieldValue> iterator() {
        return elements.iterator();
    }

    public Stream<FieldValue> stream() {
        return elements.stream();
    }

    @Override
    public boolean isLink() {
        return false;
    }
}
