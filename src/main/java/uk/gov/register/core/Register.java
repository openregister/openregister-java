package uk.gov.register.core;

import org.jvnet.hk2.annotations.Contract;

@Contract
public interface Register extends RegisterReadOnly {
    void addItem(Item item); // TODO: what about batches?
    void addEntry(Entry entry); // record handled by this automatically. is it right that user specifies entry number?
    void addItemAndEntry(Item item, Entry entry);
}
