package uk.gov.register.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileWriter {
    public static File writeToFile(String path, byte[] buffer) throws IOException {
        File file = new File(path);

        FileOutputStream registersOutputStream = new FileOutputStream(file);
        registersOutputStream.write(buffer);

        return file;
    }
}