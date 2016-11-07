package uk.gov.register.util;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Throwables;
import io.dropwizard.jackson.Jackson;
import uk.gov.register.core.Entry;
import uk.gov.register.core.Item;
import uk.gov.register.core.RegisterDetail;

import javax.ws.rs.core.StreamingOutput;
import java.io.*;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ArchiveCreator {

    public StreamingOutput create(RegisterDetail registerDetail, Collection<Entry> entries, Collection<Item> items) {
        return output -> {
            try (ZipEntryWriter zipEntryWriter = new ZipEntryWriter(output)) {

                zipEntryWriter.writeEntry("register.json", registerDetail);

                items.forEach(item ->
                        zipEntryWriter.writeEntry(String.format("item/%s.json", item.getSha256hex()), item.getContent())
                );
                entries.forEach(entry ->
                        zipEntryWriter.writeEntry(String.format("entry/%s.json", entry.getEntryNumber()), entry)
                );
            }
        };
    }

    public StreamingOutput createRSF(Iterator<Item> items, Iterator<Entry> entries) {
        CanonicalJsonMapper canonicalJsonMapper = new CanonicalJsonMapper();
        return output -> {
            items.forEachRemaining(item -> {
                try {
                    output.write(String.format("add-item\t%s\n", canonicalJsonMapper.writeToString(item.getContent())).getBytes());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });

            entries.forEachRemaining(entry -> {
                try {
                    output.write(String.format("append-entry\t%s\t%s\n", entry.getTimestampAsISOFormat(), entry.getItemHash()).getBytes());

                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        };
    }

    private static class ZipEntryWriter implements Closeable {

        private final ZipOutputStream zipOutputStream;
        private final ObjectMapper objectMapper = Jackson.newObjectMapper();

        public ZipEntryWriter(OutputStream outputStream) {
            zipOutputStream = new ZipOutputStream(outputStream);
            objectMapper.configure(JsonGenerator.Feature.AUTO_CLOSE_TARGET, false);
        }

        private void writeEntry(String entryName, Object value) {
            try {
                ZipEntry ze = new ZipEntry(entryName);
                zipOutputStream.putNextEntry(ze);
                objectMapper.writeValue(zipOutputStream, value);
                zipOutputStream.closeEntry();
            } catch (IOException e) {
                Throwables.propagate(e);
            }
        }

        @Override
        public void close() throws IOException {
            zipOutputStream.flush();
            zipOutputStream.close();
        }
    }
}

