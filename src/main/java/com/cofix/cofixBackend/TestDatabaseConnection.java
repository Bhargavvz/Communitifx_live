package com.cofix.cofixBackend;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * A standalone class to test database connection.
 * Run this class directly to verify database connectivity.
 */
public class TestDatabaseConnection {

    // Database credentials from application.properties
    private static final String URL = "jdbc:postgresql://dpg-cvqb6i6uk2gs73d1glo0-a.oregon-postgres.render.com:5432/cofixdb_zvjo";
    private static final String USER = "cofixdb_zvjo_user";
    private static final String PASSWORD = "NPNnwgJEE0fBbo1Wd9lpu3xySgnDVp28";

    public static void main(String[] args) {
        System.out.println("Testing database connection...");
        
        // Load the PostgreSQL JDBC driver
        try {
            Class.forName("org.postgresql.Driver");
            System.out.println("PostgreSQL JDBC Driver loaded successfully");
        } catch (ClassNotFoundException e) {
            System.err.println("Failed to load PostgreSQL JDBC driver");
            e.printStackTrace();
            return;
        }

        // Set up connection properties
        Properties props = new Properties();
        props.setProperty("user", USER);
        props.setProperty("password", PASSWORD);
        props.setProperty("ssl", "true");
        props.setProperty("sslmode", "require");
        props.setProperty("ApplicationName", "CoFixBackend-Test");

        // Try to establish a connection
        try (Connection connection = DriverManager.getConnection(URL, props)) {
            if (connection != null) {
                System.out.println("Database connection successful!");
                System.out.println("Connection details:");
                System.out.println("  - URL: " + URL);
                System.out.println("  - User: " + USER);
                System.out.println("  - Auto-commit: " + connection.getAutoCommit());
                System.out.println("  - Catalog: " + connection.getCatalog());
                System.out.println("  - Transaction isolation: " + connection.getTransactionIsolation());
                System.out.println("  - Connection valid: " + connection.isValid(5));
            } else {
                System.err.println("Failed to establish connection - connection is null");
            }
        } catch (SQLException e) {
            System.err.println("Database connection failed!");
            System.err.println("Error message: " + e.getMessage());
            System.err.println("SQL State: " + e.getSQLState());
            System.err.println("Error code: " + e.getErrorCode());
            e.printStackTrace();
        }
    }
}
