package uk.gov.register.util;

import org.junit.Test;
import uk.gov.register.core.RegisterId;
import uk.gov.register.core.RegisterMetadata;

import java.util.Arrays;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

public class RegisterComparerTest {
    @Test
    public void equals_shouldReturnFalse_whenPhaseDiffers() {
        RegisterMetadata one = new RegisterMetadata(
                new RegisterId("register"), Arrays.asList(), "copyright", "registry", "text", "beta");
        RegisterMetadata two = new RegisterMetadata(
                new RegisterId("register"), Arrays.asList(), "copyright", "registry", "text", "alpha");
         assertThat(RegisterComparer.equals(one,two), equalTo(false));
    }

    @Test
    public void equals_shouldReturnFalse_whenCopyrightDiffers() {
        RegisterMetadata one = new RegisterMetadata(
                new RegisterId("register"), Arrays.asList(), "copyright", "registry", "text", "alpha");
        RegisterMetadata two = new RegisterMetadata(
                new RegisterId("register"), Arrays.asList(), "edit copyright", "registry", "text", "alpha");
         assertThat(RegisterComparer.equals(one,two), equalTo(false));
    }

    @Test
    public void equals_shouldReturnFalse_whenRegistryDiffers() {
        RegisterMetadata one = new RegisterMetadata(
                new RegisterId("register"), Arrays.asList(), "copyright", "registry", "text", "alpha");
        RegisterMetadata two = new RegisterMetadata(
                new RegisterId("register"), Arrays.asList(), "copyright", "edit registry", "text", "alpha");
         assertThat(RegisterComparer.equals(one,two), equalTo(false));
    }

    @Test
    public void equals_shouldReturnFalse_whenRegisterNameDiffers() {
        RegisterMetadata one = new RegisterMetadata(
                new RegisterId("register"), Arrays.asList(), "copyright", "registry", "text", "alpha");
        RegisterMetadata two = new RegisterMetadata(
                new RegisterId("register edit"), Arrays.asList(), "copyright", "registry", "text", "alpha");
         assertThat(RegisterComparer.equals(one,two), equalTo(false));
    }

    @Test
    public void equals_shouldReturnFalse_whenSingleRegisterHasNullFields(){
        RegisterMetadata one = new RegisterMetadata(
                new RegisterId("register"), null, "copyright", "registry", "text", "alpha");
        RegisterMetadata two = new RegisterMetadata(
                new RegisterId("register"), Arrays.asList("FieldA"), "copyright", "registry", "text", "alpha");
        assertThat(RegisterComparer.equals(one,two), equalTo(false));
    }

    @Test
    public void equals_shouldReturnFalse_whenFieldsForBothRegistersAreDefinedButDiffer(){
        RegisterMetadata one = new RegisterMetadata(
                new RegisterId("register"), Arrays.asList("FieldA"), "copyright", "registry", "text", "alpha");
        RegisterMetadata two = new RegisterMetadata(
                new RegisterId("register"), Arrays.asList("FieldB"), "copyright", "registry", "text", "alpha");
        assertThat(RegisterComparer.equals(one,two), equalTo(false));
    }

    @Test
    public void equals_shouldReturnTrue_whenBothRegistersHaveNullFields(){
        RegisterMetadata one = new RegisterMetadata(
                new RegisterId("register"), null, "copyright", "registry", "text", "alpha");
        RegisterMetadata two = new RegisterMetadata(
                new RegisterId("register"), null, "copyright", "registry", "text", "alpha");
        assertThat(RegisterComparer.equals(one,two), equalTo(true));
    }

    @Test
    public void equals_shouldReturnTrue_whenRegistersMatchButTextDiffers() {
        RegisterMetadata one = new RegisterMetadata(
                new RegisterId("register"), Arrays.asList("FieldA"), "copyright", "registry", "text", "alpha");
        RegisterMetadata two = new RegisterMetadata(
                new RegisterId("register"), Arrays.asList("FieldA"), "copyright", "registry", "updated text", "alpha");
        assertThat(RegisterComparer.equals(one,two), equalTo(true));
    }

    @Test
    public void equals_shouldReturnTrue_whenRegistersMatch(){
        RegisterMetadata one = new RegisterMetadata(
                new RegisterId("register"), Arrays.asList("FieldA"), "copyright", "registry", "text", "alpha");
        RegisterMetadata two = new RegisterMetadata(
                new RegisterId("register"), Arrays.asList("FieldA"), "copyright", "registry", "text", "alpha");
        assertThat(RegisterComparer.equals(one,two), equalTo(true));
    }
}
