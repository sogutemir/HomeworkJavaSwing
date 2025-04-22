package com.arel.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Veritabanı bağlantısını yöneten singleton sınıf
 */
public class DatabaseConnection {
    private static final String DB_URL = "jdbc:postgresql://localhost:5432/akademik_randevu";
    private static final String DB_USER = "postgres";
    private static final String DB_PASSWORD = "123456";
    
    private static DatabaseConnection instance;
    private Connection connection;
    
    private DatabaseConnection() {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("PostgreSQL JDBC sürücüsü bulunamadı: " + e.getMessage());
        }
    }
    
    public static synchronized DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }
    
    public Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            } catch (SQLException e) {
                System.err.println("Veritabanı bağlantısı kurulamadı: " + e.getMessage());
                throw e;
            }
        }
        return connection;
    }
    
    public void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                System.out.println("Veritabanı bağlantısı kapatıldı.");
            } catch (SQLException e) {
                System.err.println("Veritabanı bağlantısı kapatılırken hata: " + e.getMessage());
            }
        }
    }
}
