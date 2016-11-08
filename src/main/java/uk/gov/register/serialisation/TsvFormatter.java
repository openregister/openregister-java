package uk.gov.register.serialisation;

import uk.gov.register.core.Entry;
import uk.gov.register.core.Item;
import uk.gov.register.util.CanonicalJsonMapper;
import uk.gov.register.views.RegisterProof;

public class TsvFormatter implements SerialisationFormatter {

    private final String TAB = "\t";
    private final String NEW_LINE = System.lineSeparator();
    private final CanonicalJsonMapper canonicalJsonMapper = new CanonicalJsonMapper();

    @Override
    public String format(Item item) {
        return "add-item" + TAB + canonicalJsonMapper.writeToString(item.getContent()) + NEW_LINE;
    }

    @Override
    public String format(Entry entry) {
        return "append-entry" + TAB + entry.getTimestampAsISOFormat() + TAB + entry.getItemHash() + NEW_LINE;
    }

    @Override
    public String format(RegisterProof proof) {
        return "assert-root-hash" + TAB + proof.getRootHash() + NEW_LINE;
    }

    @Override
    public String getFileExtension() {
        return "tsv";
    }
}
