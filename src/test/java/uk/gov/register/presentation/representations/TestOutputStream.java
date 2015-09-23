package uk.gov.register.presentation.representations;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

class TestOutputStream extends OutputStream {
    String contents = "";

    @Override
    public void write(int bytes) throws IOException {
        //ignore
    }

    @Override
    public void write(byte[] b) throws IOException {
        contents += new String(b, StandardCharsets.UTF_8);
    }
}
