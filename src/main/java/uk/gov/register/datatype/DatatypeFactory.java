package uk.gov.register.datatype;

public class DatatypeFactory {
    public static Datatype get(String datatype) {
        if ("integer".equals(datatype))
            return new IntegerDatatype();
        else if ("point".equals(datatype) )
            return new PointDatatype();
        else if ("url".equals(datatype) )
            return new UrlDatatype();
        else if ("curie".equals(datatype) || "string".equals(datatype) || "text".equals(datatype))
            return new StringDatatype();
        else
            return new UnvalidatedDatatype();
    }
}
