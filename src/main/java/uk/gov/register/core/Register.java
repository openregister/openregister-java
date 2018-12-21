package uk.gov.register.core;

import org.jvnet.hk2.annotations.Contract;
import uk.gov.register.exceptions.AppendEntryException;

@Contract
public interface Register extends RegisterReadOnly {
    void addItem(Item item);

    void appendEntry(Entry entry) throws AppendEntryException;
}
