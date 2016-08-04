package uk.gov.register.core;

import org.junit.Test;

import java.time.Instant;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class RegisterDetailTest {
    @Test
    public void getLastUpdatedTime_returnsTheTimestampIfExists() {
        RegisterDetail registerDetail = new RegisterDetail("", 0, 0, 0, Optional.of(Instant.ofEpochMilli(1411111111)), null);
        assertThat(registerDetail.getLastUpdatedTime(), equalTo("1970-01-17T07:58:31Z"));
    }

    @Test
    public void getLastUpdatedTime_returnsEmptyStringIfDoesNOTExists() {
        RegisterDetail registerDetail = new RegisterDetail("", 0, 0, 0, Optional.empty(), null);
        assertThat(registerDetail.getLastUpdatedTime(), is(nullValue()));
    }


}
