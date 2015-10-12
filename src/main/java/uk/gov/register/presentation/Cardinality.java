package uk.gov.register.presentation;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.google.common.collect.ImmutableMap;

import java.util.Map;

public enum Cardinality {
    ONE("1"),MANY("n");

    private final String id;

    Cardinality(String id) {
        this.id = id;
    }

    private static final Map<String,Cardinality> cardinalityMap = ImmutableMap.of(
            ONE.getId(),  ONE,
            MANY.getId(), MANY
    );

    /**
     * this method shouldn't be needed -- the @JsonValue annotation has a special case for enums that
     * means it can be used for both serialization and deserialization -- but there's a bug.
     * See <a href="https://github.com/dropwizard/dropwizard/issues/699">dropwizard issue #699</a>
     */
    @JsonCreator
    public static Cardinality fromId(String id) {
        return cardinalityMap.get(id);
    }

    @JsonValue
    public String getId() {
        return id;
    }
}
