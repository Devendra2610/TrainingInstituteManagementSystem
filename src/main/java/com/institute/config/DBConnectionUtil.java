package com.institute.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Singleton class for handling Database Connections.
 * In a production environment, this should wrap a Connection Pool like HikariCP or Tomcat JDBC Pool.
 */
public class DBConnectionUtil {
    private static DBConnectionUtil instance;
    private Connection connection;

    private static final String URL = "jdbc:mysql://localhost:3306/institute_db";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "Root";

    private DBConnectionUtil() throws SQLException {
        try {
            // Load MySQL JDBC Driver
            Class.forName("com.mysql.cj.jdbc.Driver");
            this.connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
        } catch (ClassNotFoundException ex) {
            System.err.println("Database Driver not found: " + ex.getMessage());
        }
    }

    public Connection getConnection() {
        return connection;
    }

    public static synchronized DBConnectionUtil getInstance() throws SQLException {
        if (instance == null || instance.getConnection() == null || instance.getConnection().isClosed()) {
            instance = new DBConnectionUtil();
        }
        return instance;
    }
}
