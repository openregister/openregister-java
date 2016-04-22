package uk.gov.register.presentation;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Throwables;
import io.dropwizard.jackson.Jackson;
import uk.gov.register.presentation.dao.Record;

import javax.ws.rs.core.StreamingOutput;
import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ArchiveCreator {

    public StreamingOutput create(RegisterDetail registerDetail, List<Record> records) {
        return output -> {
            try (ZipEntryWriter zipEntryWriter = new ZipEntryWriter(output)) {

                zipEntryWriter.writeEntry("register.json", registerDetail);

                records.stream()
                        .collect(Collectors.toMap(e -> e.entry.getSha256hex(), e -> e))
                        .forEach((hash, d) -> {
                                    zipEntryWriter.writeEntry(String.format("item/%s.json", hash), d.item.content);

                                    zipEntryWriter.writeEntry(String.format("entry/%s.json", d.entry.entryNumber), new ArchiveEntryData(d.entry.entryNumber, d.entry.getSha256hex(), d.entry.getTimestamp()));
                                }
                        );
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
                Throwables.propagate(e);
            }
        }

        @Override
        public void close() throws IOException {
            zipOutputStream.flush();
            zipOutputStream.close();
        }
    }

    private static class ArchiveEntryData {
        public final String entryNumber;
        public final String itemHash;
        public final String timestamp;

        public ArchiveEntryData(String entryNumber, String itemHash, String timestamp) {
            this.entryNumber = entryNumber;
            this.itemHash = itemHash;
            this.timestamp = timestamp;
        }
    }
}

