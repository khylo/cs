package com.example.demo;

import com.example.demo.model.LogEntry;
import com.example.demo.repository.LogEntryRepoIF;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.File;
import java.io.IOException;

public class JacksonJpaProcessorTest{
    @Test
    public void testProcessGood() throws Exception {
        JacksonJpaProcessor p = new JacksonJpaProcessor();
        p.setFile(new File(this.getClass().getClassLoader().getResource("test.json" ).toURI()));
        LogEntryRepoIF mockRepo = mock(LogEntryRepoIF.class);

        p.setRepo(mockRepo);

        // Run process
        p.process();

        verify(mockRepo, times(3)).save(isA(LogEntry.class));
    }

    @Test
    public void testDuplicateEntry() throws Exception {
        JacksonJpaProcessor p = new JacksonJpaProcessor();
        p.setFile(new File(this.getClass().getClassLoader().getResource("duplicate.json" ).toURI()));
        LogEntryRepoIF mockRepo = mock(LogEntryRepoIF.class);

        p.setRepo(mockRepo);

        // Run process
        p.process();

        verify(mockRepo, times(1)).save(isA(LogEntry.class));
    }

    @Test
    public void testInvalid() throws Exception {
        JacksonJpaProcessor p = new JacksonJpaProcessor();
        p.setFile(new File(this.getClass().getClassLoader().getResource("invalid.json" ).toURI()));
        LogEntryRepoIF mockRepo = mock(LogEntryRepoIF.class);

        p.setRepo(mockRepo);

        // Run process
        assertThrows(IOException.class, p::process);
    }

    @Test
    public void testAddToStore() throws Exception {
        JacksonJpaProcessor p = new JacksonJpaProcessor();
        LogEntryRepoIF mockRepo = mock(LogEntryRepoIF.class);
        p.setRepo(mockRepo);

        ArgumentCaptor<LogEntry> logEntryArg = ArgumentCaptor.forClass(LogEntry.class);

        //Verify store empty
        assertEquals(0, p.getStore().size());

        // Add one
        p.addToStore(LogEntry.builder().id("id1").state(LogEntry.State.STARTED).timestamp(1L).build());

        //verify 1 entry in store
        assertEquals(1, p.getStore().size());
        assertEquals("id1", p.getStore().keySet().iterator().next());

        //Add same entry again (unexpected case)
        p.addToStore(LogEntry.builder().id("id1").state(LogEntry.State.STARTED).timestamp(1L).build());

        // Verify size still 1
        assertEquals(1, p.getStore().size());
        assertEquals("id1", p.getStore().keySet().iterator().next());

        //Add finished entry
        p.addToStore(LogEntry.builder().id("id1").state(LogEntry.State.FINISHED).timestamp(2L).build());

        //Verify store empty
        assertEquals(0, p.getStore().size());

        //Verify save called
        verify(mockRepo).save(logEntryArg.capture());
        assertEquals("id1", logEntryArg.getValue().getId());
        assertEquals(1, logEntryArg.getValue().getDuration());
        assertTrue(!logEntryArg.getValue().isAlert());

    }
}
