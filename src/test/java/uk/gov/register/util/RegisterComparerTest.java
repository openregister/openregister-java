package uk.gov.register.util;

import org.junit.Test;
import uk.gov.register.core.RegisterMetadata;
import uk.gov.register.core.RegisterName;

import java.util.Arrays;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

public class RegisterComparerTest {
    @Test
    public void equals_shouldReturnFalseIfPhaseDiffers() {
        RegisterMetadata one = new RegisterMetadata(
                new RegisterName("Hello"), Arrays.asList(), "foo", "bar", "baz", "beta");
        RegisterMetadata two = new RegisterMetadata(
                new RegisterName("Hello"), Arrays.asList(), "foo", "bar", "baz", "alpha");
         assertThat(RegisterComparer.equals(one,two), equalTo(false));
    }

    @Test
    public void equals_shouldReturnFalseIfCopyrightDiffers() {
        RegisterMetadata one = new RegisterMetadata(
                new RegisterName("Hello"), Arrays.asList(), "foo", "bar", "baz", "alpha");
        RegisterMetadata two = new RegisterMetadata(
                new RegisterName("Hello"), Arrays.asList(), "bar", "bar", "baz", "alpha");
         assertThat(RegisterComparer.equals(one,two), equalTo(false));
    }

    @Test
    public void equals_shouldReturnFalseIfRegistryDiffers() {
        RegisterMetadata one = new RegisterMetadata(
                new RegisterName("Hello"), Arrays.asList(), "foo", "foo", "baz", "alpha");
        RegisterMetadata two = new RegisterMetadata(
                new RegisterName("Hello"), Arrays.asList(), "bar", "bar", "baz", "alpha");
         assertThat(RegisterComparer.equals(one,two), equalTo(false));
    }

    @Test
    public void equals_shouldReturnFalseIfRegisterNameDiffers() {
        RegisterMetadata one = new RegisterMetadata(
                new RegisterName("Goodbye"), Arrays.asList(), "foo", "foo", "baz", "alpha");
        RegisterMetadata two = new RegisterMetadata(
                new RegisterName("Hello"), Arrays.asList(), "foo", "bar", "baz", "alpha");
         assertThat(RegisterComparer.equals(one,two), equalTo(false));
    }

    @Test
    public void equals_shouldReturnFalseIfFieldsDiffer(){
        RegisterMetadata one = new RegisterMetadata(
                new RegisterName("Goodbye"), Arrays.asList("FieldA"), "foo", "bar", "baz", "alpha");
        RegisterMetadata two = new RegisterMetadata(
                new RegisterName("Hello"), Arrays.asList("FieldB"), "foo", "bar", "baz", "alpha");
        assertThat(RegisterComparer.equals(one,two), equalTo(false));
    }

    @Test
    public void equals_shouldReturnTrueIfRegistersMatch(){
        RegisterMetadata one = new RegisterMetadata(
                new RegisterName("Goodbye"), Arrays.asList("FieldA"), "foo", "bar", "baz", "alpha");
        RegisterMetadata two = new RegisterMetadata(
                new RegisterName("Goodbye"), Arrays.asList("FieldA"), "foo", "bar", "baz", "alpha");
        assertThat(RegisterComparer.equals(one,two), equalTo(true));
    }

    @Test
    public void equals_shouldReturnFalseIfFieldIsNull(){
        RegisterMetadata one = new RegisterMetadata(
                new RegisterName("Goodbye"), null, "foo", "bar", "baz", "alpha");
        RegisterMetadata two = new RegisterMetadata(
                new RegisterName("Goodbye"), Arrays.asList("FieldA"), "foo", "bar", "baz", "alpha");
        assertThat(RegisterComparer.equals(one,two), equalTo(false));
    }
    @Test
    public void equals_shouldReturnTrueIfBothFieldsNull(){
        RegisterMetadata one = new RegisterMetadata(
                new RegisterName("Goodbye"), null, "foo", "bar", "baz", "alpha");
        RegisterMetadata two = new RegisterMetadata(
                new RegisterName("Goodbye"), null, "foo", "bar", "baz", "alpha");
        assertThat(RegisterComparer.equals(one,two), equalTo(true));
    }

}
