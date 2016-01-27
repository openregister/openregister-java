package uk.gov.register.presentation;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import uk.gov.register.proofs.ct.SignedTreeHead;

import javax.ws.rs.core.StreamingOutput;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ArchiveCreator {
    public StreamingOutput create(List<DbEntry> entries, SignedTreeHead sth) {
        return output -> {
            ZipOutputStream zos = new ZipOutputStream(output);

            ZipEntry ze = new ZipEntry("register.txt");
            zos.putNextEntry(ze);
            zos.write("This will contain the /register data in JSON".getBytes());
            zos.closeEntry();

            ze = new ZipEntry("proof.json");
            zos.putNextEntry(ze);
            Proofs proofs = new Proofs(sth);
            ObjectMapper om = new ObjectMapper();
            om.configure(JsonGenerator.Feature.AUTO_CLOSE_TARGET, false);
            om.writeValue(zos, proofs);
            zos.closeEntry();

            entries.forEach(singleEntry -> {
                JsonNode entryData = singleEntry.getContent().getContent();
                ZipEntry singleZipEntry = new ZipEntry(String.format("item/%s.json", singleEntry.getSerialNumber()));
                try {
                    zos.putNextEntry(singleZipEntry);
                    zos.write(entryData.toString().getBytes(StandardCharsets.UTF_8));
                    zos.closeEntry();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
            zos.flush();
            zos.close();
        };
    }
}
