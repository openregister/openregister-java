package uk.gov.register.presentation;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

public class ListValue implements FieldValue, Iterable<FieldValue> {
    private List<FieldValue> elements;

    public ListValue(Iterable<FieldValue> elements) {
        this.elements = ImmutableList.copyOf(elements);
    }

    public boolean isList() {
        return true;
    }

    @Override
    @JsonIgnore
    public String getValue() {
        return "[ " + String.join(", ", Lists.transform(elements, FieldValue::getValue)) + " ]";
    }

    @Override
    public Iterator<FieldValue> iterator() {
        return elements.iterator();
    }

    public Stream<FieldValue> stream() {
        return elements.stream();
    }

    public boolean isLink() {
        return false;
    }
}
