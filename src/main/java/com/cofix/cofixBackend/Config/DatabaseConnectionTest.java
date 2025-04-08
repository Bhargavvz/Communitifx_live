package com.cofix.cofixBackend.Config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

@Configuration
public class DatabaseConnectionTest {

    @Bean
    @Profile("test-db-connection")
    public CommandLineRunner testDatabaseConnection() {
        return args -> {
            System.out.println("Testing database connection...");
            
            String url = "jdbc:postgresql://dpg-cvqb6i6uk2gs73d1glo0-a.oregon-postgres.render.com:5432/cofixdb_zvjo";
            String username = "cofixdb_zvjo_user";
            String password = "NPNnwgJEE0fBbo1Wd9lpu3xySgnDVp28";
            
            try {
                // Load the PostgreSQL JDBC driver
                Class.forName("org.postgresql.Driver");
                
                // Attempt to establish a connection
                System.out.println("Connecting to database...");
                Connection connection = DriverManager.getConnection(url, username, password);
                
                System.out.println("Database connection successful!");
                System.out.println("Connection info: " + connection.getMetaData().getDatabaseProductName() + " " + 
                                  connection.getMetaData().getDatabaseProductVersion());
                
                // Close the connection
                connection.close();
                System.out.println("Connection closed.");
            } catch (ClassNotFoundException e) {
                System.err.println("PostgreSQL JDBC driver not found: " + e.getMessage());
            } catch (SQLException e) {
                System.err.println("Database connection failed: " + e.getMessage());
                e.printStackTrace();
            }
        };
    }
}
