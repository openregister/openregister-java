package uk.gov.register.service;

import org.junit.Rule;
import org.junit.experimental.theories.DataPoint;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import static org.hamcrest.Matchers.isOneOf;
import static org.hamcrest.Matchers.not;
import static org.junit.Assume.assumeFalse;
import static org.junit.Assume.assumeNotNull;
import static org.junit.Assume.assumeThat;

@RunWith(Theories.class)
public class EntryValidatorTest {
    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @DataPoint
    public static String NULL = null;

    @DataPoint
    public static String EMPTY = "";

    @DataPoint
    public static String EMOJI = "HELLO-❤️-WORLD";

    @DataPoint
    public static String NON_ASCII_LETTERS = "cliché";

    @DataPoint
    public static String NUMBER = "1";

    @DataPoint
    public static String COUNTRY_CODE = "GB";

    @DataPoint
    public static String LONG_NUMBER = "01";

    @DataPoint
    public static String NUMBER_WITH_DOT = "10.5";

    @DataPoint
    public static String LETTERS_WITH_HYPHEN = "CA-ZX";

    @DataPoint
    public static String SNAKE_CASE = "an_id";

    @DataPoint
    public static String LEGACY_SEPARATOR = "10.2/3";

    @DataPoint
    public static String LEADING_UNDERSCORE = "_1";

    @DataPoint
    public static String LEADING_DOT = ".34";

    @DataPoint
    public static String CONSECUTIVE_DOTS = "A..B";

    @DataPoint
    public static String CONSECUTIVE_HYPHENS = "ALPHA--";

    @DataPoint
    public static String CONSECUTIVE_UNDERSCORES = "C__34";

    @DataPoint
    public static String CONSECUTIVE_RESTRICTED = "C_/34";

    @Theory
    public void leadingCharactersMustBeAlphanumeric(String key) {
        assumeNotNull(key);
        assumeThat(key, not(""));
        assumeFalse(startsWithAlphanumeric(key));

        exception.expect(RuntimeException.class);

        EntryValidator.validateKey(key);
    }

    @Theory
    public void asciiOnly(String key) {
        assumeThat(key, isOneOf(EMOJI, NON_ASCII_LETTERS));

        exception.expect(RuntimeException.class);

        EntryValidator.validateKey(key);
    }

    @Theory
    public void noConsecutiveRestrictedCharacters(String key) {
        assumeThat(key, isOneOf(CONSECUTIVE_DOTS, CONSECUTIVE_HYPHENS, CONSECUTIVE_RESTRICTED, CONSECUTIVE_UNDERSCORES));

        exception.expect(RuntimeException.class);

        EntryValidator.validateKey(key);
    }

    @Theory
    public void alphanumericAllowed(String key) {
        assumeThat(key, isOneOf(NUMBER, COUNTRY_CODE, LONG_NUMBER));

        EntryValidator.validateKey(key);
    }

    @Theory
    public void separatorsAllowedInMiddle(String key) {
        assumeThat(key, isOneOf(NUMBER_WITH_DOT, LETTERS_WITH_HYPHEN, SNAKE_CASE, LEGACY_SEPARATOR));

        EntryValidator.validateKey(key);
    }

    private Boolean startsWithAlphanumeric(String key) {
        return key.matches("[A-Za-z0-9].*");
    }

}
