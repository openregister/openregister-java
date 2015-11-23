package uk.gov.register.presentation;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

public class EntryView {
    private final int serialNumber;
    private final String hash;
    private final String registerName;
    private final Map<String, FieldValue> entryMap;

    public EntryView(int serialNumber, String hash, String registerName, Map<String, FieldValue> entryMap) {
        this.hash = hash;
        this.registerName = registerName;
        this.entryMap = new TreeMap<>(entryMap); // TreeMap so we get fields in sorted order
        this.serialNumber = serialNumber;
    }


    @JsonProperty("entry")
    public Map<String, FieldValue> getContent() {
        return entryMap;
    }

    @JsonProperty("hash")
    public String getHash() {
        return hash;
    }

    public Optional<FieldValue> getField(String fieldName) {
        return Optional.ofNullable(entryMap.get(fieldName));
    }

    @JsonProperty("serial-number")
    public int getSerialNumber() {
        return serialNumber;
    }

    @SuppressWarnings("unused, used from html templates")
    public String primaryKey() {
        return entryMap.get(registerName).getValue();
    }

    @SuppressWarnings("unused, used from html templates")
    public String registerName() {
        return registerName;
    }

    @SuppressWarnings("unused, used from html templates")
    public String getAuditPath() {
        // unimplemented; hardcoded value for now
        return "[\n" +
                "    \"99t31KVfWjz8ix0pWRT0KTUKJauAtc4TH1nhtyyvRjA=\",\n" +
                "    \"IRFw6DVWcNgwxDbXhCuIuN6y51t/pfJBgKjkcJRlrEw=\",\n" +
                "    \"DlqcXhFIdgUICJdA17917ph1vHSTXOzw/G2SXecJNTA=\",\n" +
                "    \"5Jj7g1Y4MYvARwgOnfhDot7DkJxdYYD6Qr8Jzl1ncTI=\",\n" +
                "    \"yEkOmJ73J4XL4Qpkv3D4qC1mN82Od/Ndbu4sXEEiOew=\",\n" +
                "    \"uswSM2HekesEAPIGdkZERYEBEZ7hawvgqLWUz9Ewkkk=\",\n" +
                "    \"fgO4O6M679Cl1qfCGmPK3YI6at1Jgy8rbWKGQ6qE2x8=\",\n" +
                "    \"5gWmZgloC8TvI6rhFHmjCepyJ7kOaspGu70+JtK6W4c=\",\n" +
                "    \"em+6WJKsyinGMrqVXLSN/bohuCw8gOyZGrinn5Q37Ek=\",\n" +
                "    \"jgSUxKvxj54SrM6U6LdCaYb8MuexWNy6P3rKXGmPFqU=\",\n" +
                "    \"NQKBgE3nKluraAwT+KLXQEcXeI4gVO48l+Hw8xlbwXA=\",\n" +
                "    \"k+v2l72v43sbi62VfVqJJNNsNrAiVG9st26MY06A5tY=\",\n" +
                "    \"8yjmDGwaIDNf4O+It4hGpXpVvXad8tD/I1qlq2P2JUQ=\",\n" +
                "    \"i8w8ouWREDi+3vvjjnX/pNstQdU2JlIO0vt9+qGXaA0=\",\n" +
                "    \"pCU8INpSyecydfBsX/haKkaUyfezlFOW2bAIqrXV8Ng=\",\n" +
                "    \"fWn9RMbuj6A3t11BfWD1zM+nKN0aKdP7WoOiEYcvd+w=\",\n" +
                "    \"SUQv6JOv8tGH82/xV63LXBpQwRfhAjchH8ComhkGa4E=\",\n" +
                "    \"yRz2PS0g52P2sRuryDE68qtSXmia/a74+NuPckpYHc0=\",\n" +
                "    \"eaCVxf1sLjsrN2D9QtYFrIM9jGJ6K4D+NKF9Lyifv34=\",\n" +
                "    \"aWa1ytHZyVlvu3EYGB7NPtlAssfijpxPzzILTe6sXD8=\",\n" +
                "    \"r454DNNoeAguA407gutu8IzNNSLlGFH2F91rqybbzFQ=\",\n" +
                "    \"zeC0N4isU4ZrwaeCJdjy31+X9avF8zt87NN6G+xOThQ=\",\n" +
                "    \"WBb5QnpttrokZ3kMCNNyAymfQK/BfPgIdsh3izVZgK0=\",\n" +
                "    \"lyzm0rW0RIi1OXHwnpQ4WVB+cbSzLplTeAWrIDZaavU=\"\n" +
                "  ]";
    }

    @SuppressWarnings("unused, used from html templates")
    public String getSignedTreeHead() {
        // unimplemented; hardcoded value for now
        return "{\n" +
                "  \"tree_size\": 9803348,\n" +
                "  \"timestamp\": 1447421303202,\n" +
                "  \"sha256_root_hash\": \"JATHxRF5gczvNPP1S1WuhD8jSx2bl+WoTt8bIE3YKvU=\",\n" +
                "  \"tree_head_signature\":\n" +
                "  \"BAMARzBFAiEAkKM3aRUBKhShdCyrGLdd8lYBV52FLrwqjHa5/YuzK7ECIFTlRmNuKLqbVQv0QS8nq0pAUwgbilKOR5piBAIC8LpS\"\n" +
                "}";
    }
}


