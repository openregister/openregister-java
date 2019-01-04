package uk.gov.register.util;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.jackson.Jackson;
import uk.gov.register.core.Entry;
import uk.gov.register.core.Item;
import uk.gov.register.core.RegisterDetail;
import uk.gov.register.views.EntryView;

import javax.ws.rs.core.StreamingOutput;
import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static com.google.common.base.Throwables.throwIfUnchecked;

public class ArchiveCreator {

    public StreamingOutput create(RegisterDetail registerDetail, Collection<Entry> entries, Collection<Item> items) {
        return output -> {
            try (ZipEntryWriter zipEntryWriter = new ZipEntryWriter(output)) {

                zipEntryWriter.writeEntry("register.json", registerDetail);

                items.forEach(item ->
                        zipEntryWriter.writeEntry(String.format("item/%s.json", item.getSha256hex().getValue()), item.getContent())
                );
                entries.forEach(entry -> {
                    EntryView entryView = new EntryView(entry);
                    zipEntryWriter.writeEntry(String.format("entry/%s.json", entry.getEntryNumber()), entryView);
                });
            }
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
                throwIfUnchecked(e);
                throw new RuntimeException(e.getCause());
            }
        }

        @Override
        public void close() throws IOException {
            zipOutputStream.flush();
            zipOutputStream.close();
        }
    }
}

