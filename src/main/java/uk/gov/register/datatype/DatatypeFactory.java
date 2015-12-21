package uk.gov.register.datatype;

public class DatatypeFactory {
    public static Datatype get(String datatypeName) {
        switch (datatypeName) {
            case "integer":
                return new IntegerDatatype(datatypeName);
            case "point":
                return new PointDatatype(datatypeName);
            case "url":
                return new UrlDatatype(datatypeName);
            case "curie":
            case "text":
            case "string":
                return new StringDatatype(datatypeName);
            case "datetime":
                return new DatetimeDatatype(datatypeName);
            default:
                return new UnvalidatedDatatype(datatypeName);
        }
    }
}
