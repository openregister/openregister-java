package uk.gov.register.util;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Throwables;
import io.dropwizard.jackson.Jackson;
import uk.gov.register.core.Entry;
import uk.gov.register.core.Item;
import uk.gov.register.core.RegisterDetail;
import uk.gov.register.serialisation.SerialisationFormatter;

import javax.ws.rs.core.StreamingOutput;
import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Iterator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ArchiveCreator {

    public StreamingOutput create(RegisterDetail registerDetail, Collection<Entry> entries, Collection<Item> items) {
        CanonicalJsonMapper mapper = new CanonicalJsonMapper();
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

    public StreamingOutput createRSF(Iterator<Item> items, Iterator<Entry> entries, SerialisationFormatter formatter) {
        return output -> {
            items.forEachRemaining(item -> {
                try {
                    output.write(formatter.format(item).getBytes());
                } catch (IOException e) {
                    Throwables.propagate(e);
                }
            });

            entries.forEachRemaining(entry -> {
                try {
                    output.write(formatter.format(entry).getBytes());
                } catch (IOException e) {
                    Throwables.propagate(e);
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

