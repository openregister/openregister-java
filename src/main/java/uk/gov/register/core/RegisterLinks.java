package uk.gov.register.core;

import java.util.List;

public class RegisterLinks {
    private final List<String> registersLinkedFrom;
    private final List<String> registersLinkedTo;

    public RegisterLinks(List<String> registersLinkedFrom, List<String> registersLinkedTo) {
        this.registersLinkedFrom = registersLinkedFrom;
        this.registersLinkedTo = registersLinkedTo;
    }

    public List<String> getRegistersLinkedFrom() {
        return registersLinkedFrom;
    }

    public List<String> getRegistersLinkedTo() {
        return registersLinkedTo;
    }
}