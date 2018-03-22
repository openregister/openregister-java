package uk.gov.register.core;

public abstract class LinkValue implements FieldValue {

    @Override
    public boolean isLink() {
        return true;
    }

    public abstract boolean isLinkToRegister();

    @Override
    public boolean isList() {
        return false;
    }
}
