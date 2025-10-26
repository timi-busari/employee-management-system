package com.employeemgt.employee.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

@Component
@Order(1) // Run before Flyway
public class DatabaseInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseInitializer.class);

    @Value("${spring.datasource.username}")
    private String username;

    @Value("${spring.datasource.password}")
    private String password;

    @Override
    public void run(String... args) throws Exception {
        createDatabaseIfNotExists();
    }

    private void createDatabaseIfNotExists() {
        String postgresUrl = "jdbc:postgresql://localhost:54321/postgres";
        String databaseName = "employee_db";

        try (Connection connection = DriverManager.getConnection(postgresUrl, username, password)) {
            // Check if database exists
            String checkQuery = "SELECT 1 FROM pg_database WHERE datname = '" + databaseName + "'";
            try (Statement statement = connection.createStatement()) {
                var resultSet = statement.executeQuery(checkQuery);
                
                if (!resultSet.next()) {
                    // Database doesn't exist, create it
                    logger.info("Creating database: {}", databaseName);
                    String createQuery = "CREATE DATABASE " + databaseName;
                    statement.executeUpdate(createQuery);
                    logger.info("Database {} created successfully", databaseName);
                } else {
                    logger.info("Database {} already exists", databaseName);
                }
            }
        } catch (SQLException e) {
            logger.warn("Could not auto-create database: {}. Please ensure database '{}' exists.", e.getMessage(), databaseName);
            logger.info("You can create it manually with: CREATE DATABASE {};", databaseName);
        }
    }
}