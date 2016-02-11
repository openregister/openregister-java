package uk.gov.register.presentation;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import uk.gov.register.proofs.ct.SignedTreeHead;

import javax.ws.rs.core.StreamingOutput;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ArchiveCreator {
    public StreamingOutput create(RegisterDetail registerDetail, List<DbEntry> entries, SignedTreeHead sth) {
        return output -> {
            ZipOutputStream zos = new ZipOutputStream(output);

            ObjectMapper om = new ObjectMapper();
            om.configure(JsonGenerator.Feature.AUTO_CLOSE_TARGET, false);

            ZipEntry ze = new ZipEntry("register.json");
            zos.putNextEntry(ze);
            om.writeValue(zos, registerDetail);
            zos.closeEntry();

            ze = new ZipEntry("proof.json");
            zos.putNextEntry(ze);
            Proofs proofs = new Proofs(sth);
            om.writeValue(zos, proofs);
            zos.closeEntry();

            // Add each item
            // Prevent duplicate files being added
            Set<String> itemHashesAdded = new HashSet<>();
            entries.forEach(singleEntry -> {
                if (!itemHashesAdded.contains(singleEntry.getContent().getHash())) {
                    JsonNode entryData = singleEntry.getContent().getContent();
                    ZipEntry singleZipEntry = new ZipEntry(String.format("item/%s.json", singleEntry.getContent().getHash()));
                    try {
                        zos.putNextEntry(singleZipEntry);
                        zos.write(entryData.toString().getBytes(StandardCharsets.UTF_8));
                        zos.closeEntry();
                        itemHashesAdded.add(singleEntry.getContent().getHash());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            });

            // Add each entry record
            // Keyed by entry, should contain entryNumber, itemHash, timestamp
            entries.forEach(singleEntry -> {
                ZipEntry singleZipEntry = new ZipEntry(String.format("entry/%d.json", singleEntry.getSerialNumber()));
                try {
                    zos.putNextEntry(singleZipEntry);

                    ArchiveEntryData aed = new ArchiveEntryData(
                            singleEntry.getSerialNumber(),
                            singleEntry.getContent().getHash(),
                            getTimestampFromPayload(singleEntry.getLeaf_input()));
                    om.writeValue(zos, aed);
                    zos.closeEntry();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
            zos.flush();
            zos.close();
        };
    }

    private long getTimestampFromPayload(String leaf_input) {
        byte[] rawdata = Base64.getDecoder().decode(leaf_input);
        byte[] rawtimestamp = Arrays.copyOfRange(rawdata, 2, 10);
        return bytesToLong(rawtimestamp);
    }

    private long bytesToLong(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.put(bytes);
        buffer.flip(); //need flip
        return buffer.getLong();
    }

    public static class ArchiveEntryData {
        public int entryNumber;
        public String itemHash;
        public long timestamp;

        public ArchiveEntryData(int entryNumber, String itemHash, long timestamp) {
            this.entryNumber = entryNumber;
            this.itemHash = itemHash;
            this.timestamp = timestamp;
        }
    }
}
