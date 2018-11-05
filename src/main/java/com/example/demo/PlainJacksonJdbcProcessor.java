package com.example.demo;

import com.example.demo.model.LogEntry;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;


/**
 * Simple implementation using Jackson and Jdbc without Spring overhead
 * To invoke call. java com.example.demo.PlainJacksonJdbcProcessor <jsonFilename>
 * Uses Jackson Streaming to read and parse the json file. Each element get converted to a LogEntry pojo. If we have not encountered the ID before it is stored in memory.
 * As we find the closing element it is removed from the in-memory store and written to the DB using JDBC.
 * JDBC connection details are loaded from the application.properties file.
 */
@Slf4j
public class PlainJacksonJdbcProcessor implements AutoCloseable {


    File file;
    @Getter
    Map<String, LogEntry> store = new HashMap<>();
    @Setter
    Connection connection;
    Properties prop;

    protected static final String SQL = "Insert into LOGENTRY (id, type, host, alert, duration) values (?,?,?,?,?)";
    protected static final String CREATE_SQL= "CREATE TABLE IF NOT EXISTS LOGENTRY (id varchar(255), state varchar(255), type varchar(255), host varchar(255), alert boolean, duration BIGINT)";


    /**
     * main method. Invoke with path to file for processing
     * @param args
     */
    public static void main(String... args){
        if(args.length<1){
                System.out.println("Usage PlainJacksonJdbcProcessor <filename>");
                System.exit(1);
            }
        File file = new File(args[0]);
        if(!file.isFile()){
                System.out.println("Cannot find "+args[0]+ " from "+System.getProperty("user.dir"));
                System.exit(1);
            }
        PlainJacksonJdbcProcessor main = null;
        try {
            main = new PlainJacksonJdbcProcessor(file);
            main.process();
        }catch(Exception e){
            System.err.println("Problem encountered running application "+e.getLocalizedMessage());
            e.printStackTrace();
            System.exit(1);
        } finally {
            main.close();
        }
    }

    /**
     * Constuctor. Takes the file to be processing in as arg. Catches any exception thrown and wraps them in a RuntimeException for handing by the main method.
     * @param file
     * @throws RuntimeException
     */
    public PlainJacksonJdbcProcessor(File file) throws RuntimeException{
        try {
            this.file = file;
            this.prop = new Properties();
            this.prop.load(this.getClass().getClassLoader().getResource("application.properties").openStream());
            connection = initConnection();
        }catch(Exception e){
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close()  {
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Uses Jackson to stream through the file. As it encounters elements it creates LogEntry objects and sends them to addToStore
     * @throws IOException
     * @throws SQLException
     */
    public void process() throws IOException, SQLException {

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
     * @throws SQLException
     */
    protected void  addToStore(LogEntry entry) throws SQLException {
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
            store.put(entry.getId(), entry);
            save(entry);
            // Clean up memory store as we save items to DB
            store.remove(entry.getId());
        }
    }

    /**
     * Initializes the JDBC connection.
     * @return
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    protected Connection initConnection() throws SQLException, ClassNotFoundException {
        // Class.forName("org.hsqldb.jdbc.JDBCDriver" );
        Connection c = DriverManager.getConnection(prop.getProperty("spring.datasource.url"), "SA", "");
        PreparedStatement ps = c.prepareStatement(CREATE_SQL);
        ps.executeUpdate();
        return c;
    }

    /**
     * Wrties the LogEntry to the DB
     * @param entry
     * @throws SQLException
     */
    protected void save(LogEntry entry)throws SQLException{
        PreparedStatement ps = connection.prepareStatement(SQL);
        ps.setString(1, entry.getId());
        ps.setString(2, entry.getType());
        ps.setString(3, entry.getHost());
        ps.setBoolean(4, entry.isAlert());
        ps.setLong(5, entry.getDuration());
        //(id, type, host, alarm, duration)
        ps.executeUpdate();
    }

}
