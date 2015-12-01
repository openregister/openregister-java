package uk.gov.register.datatype;

public class DatatypeFactory {
    public static Datatype get(String datatype) {
        if ("int".equals(datatype))
            return new IntegerDatatype();
        else if ("curie".equals(datatype))
            return new CurieDatatype();
        else if ("string".equals(datatype) || "text".equals(datatype))
            return new StringDatatype();
        else
            return new DummyDatatype();
    }
}
