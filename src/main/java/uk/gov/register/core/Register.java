package uk.gov.register.core;

import org.jvnet.hk2.annotations.Contract;

import java.util.Map;

@Contract
public interface Register extends RegisterReadOnly {
    void putItem(Item item);
    void appendEntry(Entry entry);

    Map<String, Field> getFieldsByName();
}
