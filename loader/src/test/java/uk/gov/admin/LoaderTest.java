package uk.gov.admin;

import org.junit.Test;

public class LoaderTest {
    @Test(expected = RuntimeException.class)
    public void loader_should_parse_empty_args() {
        final String[] args = new String[0];
        final LoaderArgsParser loaderArgsParser = new LoaderArgsParser();
        final LoaderArgsParser.LoaderArgs loaderArgs = loaderArgsParser.parseArgs(args);
    }

    @Test(expected = RuntimeException.class)
    public void loader_should_not_parse_null_args() {
        final String[] args = null;
        final LoaderArgsParser loaderArgsParser = new LoaderArgsParser();
        final LoaderArgsParser.LoaderArgs loaderArgs = loaderArgsParser.parseArgs(args);
    }
}
