package uk.gov.register.core.datatype;

public abstract class AbstractDatatype implements Datatype {

    private final String datatypeName;

    public AbstractDatatype(final String datatypeName) {
        this.datatypeName = datatypeName;
    }

    @Override
    public String getName() {
        return datatypeName;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final AbstractDatatype that = (AbstractDatatype) o;

        return datatypeName != null ? datatypeName.equals(that.datatypeName) : that.datatypeName == null;
    }

    @Override
    public int hashCode() {
        return datatypeName != null ? datatypeName.hashCode() : 0;
    }
}
