package uk.gov.register.util;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.register.core.Entry;
import uk.gov.register.core.Item;
import uk.gov.register.exceptions.SerializedRegisterParseException;
import uk.gov.register.serialization.RegisterComponents;

import java.io.*;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class SerializedRegisterParser {

    private static final Logger LOG = LoggerFactory.getLogger(SerializedRegisterParser.class);

    public RegisterComponents parseCommands(InputStream commandStream) {
        BufferedReader buffer = new BufferedReader(new InputStreamReader(commandStream));
        AtomicInteger currentEntryNumber = new AtomicInteger(0);

        List<Object> itemEntries = buffer.lines().map(s -> {
            LOG.debug("processing: " + s);
            String[] parts = s.split("\t");
            String commandName = parts[0];
            switch (commandName) {
                case "add-item":
                    if (parts.length == 2) {
                        try {
                            return new Item(new ObjectMapper().readTree(parts[1]));
                        } catch (JsonParseException jpe){
                            LOG.error("failed to parse json: " + parts[1]);
                            throw new SerializedRegisterParseException("failed to parse json: " + parts[1], jpe);
                        } catch (IOException e) {
                            LOG.error("",e);
                            throw new UncheckedIOException(e);
                        }
                    } else {
                        LOG.error("add item line must have 2 elements, was: " + s);
                        throw new SerializedRegisterParseException("add item line must have 2 elements, was: " + s);
                    }
                case "append-entry":
                    if (parts.length == 3) {
                        return new Entry(currentEntryNumber.getAndIncrement(), stripPrefix(parts[2]), Instant.parse(parts[1]));
                    } else {
                        LOG.error("append entry line must have 3 elements, was : " + s);
                        throw new SerializedRegisterParseException("append entry line must have 3 elements, was : " + s);
                    }
                default:
                    LOG.error("line must begin with legal command not:" + commandName);
                    throw new SerializedRegisterParseException("line must begin with legal command not:" + commandName);
            }
        }).collect(Collectors.toList());

        Set<Item> items = Sets.newHashSet(Iterables.filter(itemEntries, Item.class));

        List<Entry> entries = Lists.newLinkedList(Iterables.filter(itemEntries, Entry.class));
        // don't close the reader as the caller will close the input stream
        return new RegisterComponents(entries, items);

    }

    private String stripPrefix(String hashField) {
        if (!hashField.startsWith("sha-256:")) {
            LOG.error("hash field must start with sha-256: not:" + hashField);
            throw new SerializedRegisterParseException("hash field must start with sha-256: not:" + hashField);
        } else {
            return hashField.substring(8);
        }
    }


}
