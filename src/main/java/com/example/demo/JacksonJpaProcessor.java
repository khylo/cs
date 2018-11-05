package com.example.demo;

import com.example.demo.model.LogEntry;
import com.example.demo.repository.LogEntryRepoIF;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Implementation using Jackson and Spring Data Jpa
 * To run from command line use the SpringBootProcessor main class wire up dependecies correctly.
 * Uses Jackson Streaming to read and parse the json file. Each element get converted to a LogEntry pojo. If we have not encountered the ID before it is stored in memory.
 * As we find the closing element it is removed from the in-memory store and written to the DB using JDBC.
 * JDBC connection details are loaded from the application.properties file.
 */
@Slf4j
@Data
@Component
public class JacksonJpaProcessor{


    protected File file;
    private Map<String, LogEntry> store = new HashMap<>();
    @Autowired
    protected LogEntryRepoIF repo;

    /**
     * Uses Jackson to stream through the file. As it encounters elements it creates LogEntry objects and sends them to addToStore
     * @throws IOException
     */
    public void process() throws IOException {
        ObjectMapper om = new ObjectMapper();
        JsonFactory jsonF = om.getJsonFactory();
        JsonParser jp = jsonF.createParser(this.file);
        JsonToken token = jp.nextToken();
        if (token != JsonToken.START_OBJECT) {
            throw new IOException("Expected data to start with an Object");
        }
        // Iterate over object fields:
        while (token!=null && token != JsonToken.END_OBJECT){
            LogEntry entry = jp.readValueAs(LogEntry.class);
            addToStore(entry);
            token = jp.nextToken();
        }

    }
    /**
     * Checks the LogEntry. If not encountered before it adds to memory store (HashMap).
     * If we've seen it before we remove it from memory and write to DB. Also checks for duplicate Ids with same state. If encountered it ignores these.
     * @param entry
     */
    protected void  addToStore(LogEntry entry){
        LogEntry storeEntry = store.get(entry.getId());
        if(storeEntry==null){
            // Id not in Store. Add
            if(log.isDebugEnabled())
                log.debug("ID: "+entry.getId()+" added to mem store.");
            store.put(entry.getId(), entry);
        }else { // Entry is already there
            LogEntry.State existingState = storeEntry.getState();
            // Expect new State to be different
            if (entry.getState() == existingState) {
                log.warn("ID: " + entry.getId() + "  has multiple entries with"+existingState+". We will ignore this record.");
                return; // Ignore duplicate  entry
            }
            entry.setDuration(Math.abs(entry.getTimestamp() - storeEntry.getTimestamp()));
            if(log.isDebugEnabled()) {
                log.debug("ID: " + entry.getId() + " writing to DB with duration " + entry.getDuration());
            }
            LogEntry response;
            store.put(entry.getId(), entry);
            try {
                response = repo.save(entry);
                System.out.println("Response from save "+response);
            } catch(Exception e) {

                System.out.println("Exception thrown on save "+e);
                e.printStackTrace();
            }
            // Clean up memory store as we save items to DB
            store.remove(entry.getId());
        }
    }

    public void report() {
        System.out.println("Response from findAll "+repo.findAll());
        Iterator i =repo.findAll().iterator();
        while(i.hasNext()){
            System.out.println(i.next());
        }
    }
}
