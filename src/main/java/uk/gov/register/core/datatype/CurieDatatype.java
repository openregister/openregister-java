package uk.gov.register.core.datatype;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.lang3.StringUtils;

public class CurieDatatype extends AbstractDatatype {

    public static final String CURIE_SEPARATOR = ":";

    public CurieDatatype(final String datatypeName) {
        super(datatypeName);
    }

    @Override
    public boolean isValid(final JsonNode value) {
        final String[] curie;

        if (StringUtils.isEmpty(value.textValue())) {
            return false;
        }

        if (value.textValue().contains(CURIE_SEPARATOR)) {
            curie = value.textValue().split(CURIE_SEPARATOR);

            if (isThereMoreThanOneSeparator(value.textValue())) {
                return false;
            }

            return curie.length == 2 && StringUtils.isNotEmpty(curie[0]) && StringUtils.isNotEmpty(curie[1]);
        }

        return true;
    }

    private boolean isThereMoreThanOneSeparator(final String text) {
        final char curieSeparator = CURIE_SEPARATOR.charAt(0);
        int counter = 0;

        for (char c : text.toCharArray()) {
            if (curieSeparator == c) {
                counter++;
            }

            if (counter > 1) {
                return true;
            }
        }

        return false;
    }
}
