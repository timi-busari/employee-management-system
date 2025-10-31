package com.employeemgt.auth.config;

import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

@Configuration
public class FlyWayConfig {
    
    @Autowired
    private Environment environment;
    
    @Bean(initMethod = "migrate")
    public Flyway flyway() {
        // First, ensure the database exists
        createDatabaseIfNotExists();
        
        // Then configure Flyway
        return Flyway.configure()
                .baselineOnMigrate(true)
                .dataSource(
                    environment.getRequiredProperty("spring.datasource.url"),
                    environment.getRequiredProperty("spring.datasource.username"),
                    environment.getRequiredProperty("spring.datasource.password"))
                .load();
    }
    
    private void createDatabaseIfNotExists() {
        String url = environment.getRequiredProperty("spring.datasource.url");
        String username = environment.getRequiredProperty("spring.datasource.username");
        String password = environment.getRequiredProperty("spring.datasource.password");
        
        // Extract connection info for postgres database
        String postgresUrl = url.replace("/auth_service_db", "/postgres");
        
        try (Connection connection = DriverManager.getConnection(postgresUrl, username, password);
             Statement statement = connection.createStatement()) {
            
            statement.executeUpdate("CREATE DATABASE auth_service_db");
            System.out.println("Database 'auth_service_db' created successfully");
            
        } catch (SQLException e) {
            if (e.getSQLState().equals("42P04")) { // Database already exists
                System.out.println("Database 'auth_service_db' already exists");
            } else {
                System.err.println("Error creating database: " + e.getMessage());
            }
        }
    }
}