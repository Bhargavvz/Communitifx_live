package com.cofix.cofixBackend.Config;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component("databaseInitializer")
@Order(0) // Highest priority to ensure this runs first
public class DatabaseInitializer {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseInitializer.class);
    private static final String SQL_SCRIPT_PATH = "artifacts/create_db_script.sql";

    @Autowired
    private DataSource dataSource;

    @PostConstruct
    public void initializeDatabase() {
        logger.info("Starting database initialization...");
        
        try (Connection connection = dataSource.getConnection()) {
            // Log database connection information for debugging
            logDatabaseInfo(connection);
            
            // Check if tables already exist
            if (tablesExist(connection)) {
                logger.info("Database tables already exist, skipping initialization");
                return;
            }
            
            // Load the SQL script from the artifacts directory
            ClassPathResource resource = new ClassPathResource(SQL_SCRIPT_PATH);
            if (!resource.exists()) {
                logger.error("SQL script not found at path: {}", SQL_SCRIPT_PATH);
                throw new RuntimeException("SQL script not found at path: " + SQL_SCRIPT_PATH);
            }
            
            // Read the SQL script content
            String sqlScript;
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream()))) {
                sqlScript = reader.lines().collect(Collectors.joining("\n"));
                logger.info("SQL script loaded successfully, size: {} characters", sqlScript.length());
            }
            
            // Try executing with Spring's ScriptUtils first
            try {
                logger.info("Executing SQL script using ScriptUtils...");
                ScriptUtils.executeSqlScript(connection, resource);
                logger.info("SQL script executed successfully using ScriptUtils");
                
                // Verify tables were created
                if (tablesExist(connection)) {
                    logger.info("Database tables verified after initialization");
                    return;
                } else {
                    logger.warn("Tables were not created successfully with ScriptUtils");
                }
            } catch (Exception e) {
                logger.warn("ScriptUtils execution failed, will try alternative methods: {}", e.getMessage());
            }
            
            // Try executing as a single batch
            try {
                logger.info("Executing SQL script as a single batch...");
                try (Statement stmt = connection.createStatement()) {
                    stmt.execute(sqlScript);
                }
                logger.info("SQL script executed successfully as a batch");
                
                // Verify tables were created
                if (tablesExist(connection)) {
                    logger.info("Database tables verified after batch execution");
                    return;
                } else {
                    logger.warn("Tables were not created successfully with batch execution");
                }
            } catch (SQLException e) {
                logger.warn("Batch execution failed, will try statement by statement: {}", e.getMessage());
            }
            
            // If batch execution fails, try statement by statement
            logger.info("Executing SQL script statement by statement...");
            
            // Split the script into individual statements
            List<String> statements = splitSqlStatements(sqlScript);
            logger.info("Split SQL script into {} statements", statements.size());
            
            // Execute each statement
            int successCount = 0;
            try (Statement stmt = connection.createStatement()) {
                for (int i = 0; i < statements.size(); i++) {
                    String sql = statements.get(i).trim();
                    if (sql.isEmpty()) {
                        continue;
                    }
                    
                    try {
                        stmt.execute(sql);
                        successCount++;
                    } catch (SQLException e) {
                        // Log the error but continue with the next statement
                        logger.error("Error executing SQL statement #{}: {}", i + 1, e.getMessage());
                        logger.debug("Failed SQL: {}", sql);
                    }
                }
            }
            
            logger.info("Database initialization completed. Successfully executed {}/{} statements", 
                    successCount, statements.size());
            
            // Final verification
            if (tablesExist(connection)) {
                logger.info("Database tables verified after statement-by-statement execution");
            } else {
                logger.error("Tables were not created successfully after all attempts");
                throw new RuntimeException("Failed to create database tables after multiple attempts");
            }
            
        } catch (SQLException | IOException e) {
            logger.error("Error during database initialization", e);
            throw new RuntimeException("Failed to initialize database", e);
        }
    }
    
    /**
     * Logs database connection information for debugging purposes
     */
    private void logDatabaseInfo(Connection connection) throws SQLException {
        DatabaseMetaData metaData = connection.getMetaData();
        logger.info("Connected to database: {} {}", 
                metaData.getDatabaseProductName(), 
                metaData.getDatabaseProductVersion());
        logger.info("JDBC Driver: {} {}", 
                metaData.getDriverName(), 
                metaData.getDriverVersion());
        logger.info("Database URL: {}", metaData.getURL());
        logger.info("Database username: {}", metaData.getUserName());
    }
    
    /**
     * Checks if the required tables already exist in the database
     */
    private boolean tablesExist(Connection connection) throws SQLException {
        DatabaseMetaData metaData = connection.getMetaData();
        
        // Check for the users table specifically
        try (ResultSet tables = metaData.getTables(null, "public", "users", null)) {
            boolean exists = tables.next();
            logger.info("Table 'users' exists: {}", exists);
            return exists;
        }
    }
    
    /**
     * Splits a SQL script into individual statements
     */
    private List<String> splitSqlStatements(String script) {
        List<String> statements = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean inString = false;
        
        for (int i = 0; i < script.length(); i++) {
            char c = script.charAt(i);
            
            // Toggle string literal mode
            if (c == '\'') {
                inString = !inString;
            }
            
            // If semicolon outside string, end of statement
            if (c == ';' && !inString) {
                sb.append(c);
                statements.add(sb.toString());
                sb = new StringBuilder();
            } else {
                sb.append(c);
            }
        }
        
        // Add the last statement if it doesn't end with a semicolon
        if (sb.length() > 0) {
            statements.add(sb.toString());
        }
        
        return statements;
    }
}
