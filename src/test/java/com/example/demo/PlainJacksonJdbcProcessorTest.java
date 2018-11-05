package com.example.demo;

import com.example.demo.model.LogEntry;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class PlainJacksonJdbcProcessorTest {
    @Test
    public void testProcessGood() throws Exception {
        PlainJacksonJdbcProcessor p = new PlainJacksonJdbcProcessor(new File(this.getClass().getClassLoader().getResource("test.json" ).toURI()));
        Connection mockConnection = mock(Connection.class);
        PreparedStatement mockPs = mock(PreparedStatement.class);
        when(mockConnection.prepareStatement(PlainJacksonJdbcProcessor.SQL)).thenReturn(mockPs);

        p.setConnection(mockConnection);

        // Run process
        p.process();

        verify(mockConnection, times(3)).prepareStatement(PlainJacksonJdbcProcessor.SQL);
    }

    @Test
    public void testDuplicateEntry() throws Exception {
        PlainJacksonJdbcProcessor p = new PlainJacksonJdbcProcessor(new File(this.getClass().getClassLoader().getResource("duplicate.json" ).toURI()));
        Connection mockConnection = mock(Connection.class);
        PreparedStatement mockPs = mock(PreparedStatement.class);
        when(mockConnection.prepareStatement(PlainJacksonJdbcProcessor.SQL)).thenReturn(mockPs);
        p.setConnection(mockConnection);

        // Run process
        p.process();

        verify(mockConnection, times(1)).prepareStatement(PlainJacksonJdbcProcessor.SQL);
    }

    @Test
    public void testInvalid() throws Exception {
        PlainJacksonJdbcProcessor p = new PlainJacksonJdbcProcessor(new File(this.getClass().getClassLoader().getResource("invalid.json" ).toURI()));
        Connection mockConnection = mock(Connection.class);

        p.setConnection(mockConnection);

        // Run process
        assertThrows(IOException.class, p::process);
    }

    @Test
    public void testAddToStore() throws Exception {
        PlainJacksonJdbcProcessor p = new PlainJacksonJdbcProcessor(null);
        Connection mockConnection = mock(Connection.class);
        PreparedStatement mockPs = mock(PreparedStatement.class);
        when(mockConnection.prepareStatement(PlainJacksonJdbcProcessor.SQL)).thenReturn(mockPs);
        p.setConnection(mockConnection);

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

        //Verify executeUpdate called
        verify(mockPs).executeUpdate();

    }
}
