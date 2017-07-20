package uk.gov.register.util;

import org.junit.Test;
import uk.gov.register.core.Cardinality;
import uk.gov.register.core.Field;
import uk.gov.register.core.RegisterName;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

public class FieldComparerTest {
    private FieldComparer fieldComparer = new FieldComparer();

    @Test
    public void equals_shouldReturnFalse_whenCardinalityIsNotEqual() throws Exception {
        Field field1 = new Field("town", "string", new RegisterName("town"), Cardinality.ONE, "description");
        Field field2 = new Field("town", "string", new RegisterName("town"), Cardinality.MANY, "description");

        assertThat(fieldComparer.equals(field1, field2), equalTo(false));
    }

    @Test
    public void equals_shouldReturnFalse_whenDatatypeIsNotEqual() throws Exception {
        Field field1 = new Field("town", "string", new RegisterName("town"), Cardinality.ONE, "description");
        Field field2 = new Field("town", "integer", new RegisterName("town"), Cardinality.ONE, "description");

        assertThat(fieldComparer.equals(field1, field2), equalTo(false));
    }

    @Test
    public void equals_shouldReturnFalse_whenFieldIsNotEqual() throws Exception {
        Field field1 = new Field("town", "string", new RegisterName("town"), Cardinality.ONE, "description");
        Field field2 = new Field("postcode", "string", new RegisterName("town"), Cardinality.ONE, "description");

        assertThat(fieldComparer.equals(field1, field2), equalTo(false));
    }

    @Test
    public void equals_shouldReturnFalse_whenRegisterNameIsNotEqual() throws Exception {
        Field field1 = new Field("town", "string", new RegisterName("town"), Cardinality.ONE, "description");
        Field field2 = new Field("town", "string", new RegisterName("postcode"), Cardinality.ONE, "description");

        assertThat(fieldComparer.equals(field1, field2), equalTo(false));
    }

    @Test
    public void equals_shouldReturnTrue_whenFieldsAreEqual() throws Exception {
        Field field1 = new Field("town", "string", new RegisterName("town"), Cardinality.ONE, "description");
        Field field2 = new Field("town", "string", new RegisterName("town"), Cardinality.ONE, "description");

        assertThat(fieldComparer.equals(field1, field2), equalTo(true));
    }

    @Test
    public void equals_shouldReturnTrue_whenFieldsAreEqualButTextIsDifferent() throws Exception {
        Field field1 = new Field("town", "string", new RegisterName("town"), Cardinality.ONE, "the town");
        Field field2 = new Field("town", "string", new RegisterName("town"), Cardinality.ONE, "a town");

        assertThat(fieldComparer.equals(field1, field2), equalTo(true));
    }
}
