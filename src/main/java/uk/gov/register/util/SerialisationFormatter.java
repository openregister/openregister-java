package uk.gov.register.util;

import uk.gov.register.core.Entry;
import uk.gov.register.core.Item;
import uk.gov.register.views.RegisterProof;

public interface SerialisationFormatter {
    String format(Item item);
    String format(Entry entry);
    String format(RegisterProof proof);

    String getFileExtension();
}

