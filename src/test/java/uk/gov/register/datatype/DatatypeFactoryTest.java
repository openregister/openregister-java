package uk.gov.register.datatype;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class DatatypeFactoryTest {
    @Test
    public void get_returnsIntegerDatatype_whenDatatypeIsInteger() {
        Datatype datatype = DatatypeFactory.get("integer");
        assertTrue(datatype instanceof IntegerDatatype);
    }

    @Test
    public void get_returnsStringDatatype_whenDatatypeIsCurie() {
        Datatype datatype = DatatypeFactory.get("curie");
        assertTrue(datatype instanceof StringDatatype);
    }

    @Test
    public void get_returnsStringDatatype_whenDatatypeIsText() {
        Datatype datatype = DatatypeFactory.get("text");
        assertTrue(datatype instanceof StringDatatype);
    }

    @Test
    public void get_returnsStringDatatype_whenDatatypeIsString() {
        Datatype datatype = DatatypeFactory.get("string");
        assertTrue(datatype instanceof StringDatatype);
    }

    @Test
    public void get_returnsUrlDatatype_whenDatatypeIsUrl() {
        Datatype datatype = DatatypeFactory.get("url");
        assertTrue(datatype instanceof UrlDatatype);
    }

    @Test
    public void get_returnsPointDatatype_whenDatatypeIsUrl() {
        Datatype datatype = DatatypeFactory.get("point");
        assertTrue(datatype instanceof PointDatatype);
    }

    @Test
    public void get_returnsUnvalidatedDatatype_whenDatatypeIsNotRecognised() {
        Datatype datatype = DatatypeFactory.get("foo");
        assertTrue(datatype instanceof UnvalidatedDatatype);
    }
}
