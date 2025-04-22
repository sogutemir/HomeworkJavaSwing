package com.arel.database;

import com.arel.model.Kullanici;
import com.arel.utils.PasswordUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Kullanıcı verilerini veritabanında saklayan ve yöneten DAO sınıfı
 */
public class KullaniciDAO {
    private final DatabaseConnection dbConnection;
    
    public KullaniciDAO() {
        this.dbConnection = DatabaseConnection.getInstance();
    }
    
    /**
     * Kullanıcı tablosunu oluşturur
     */
    public void createTable() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS kullanicilar (" +
                    "id SERIAL PRIMARY KEY, " +
                    "ad VARCHAR(100) NOT NULL, " +
                    "soyad VARCHAR(100) NOT NULL, " +
                    "kullanici_adi VARCHAR(50) NOT NULL UNIQUE, " +
                    "sifre VARCHAR(100) NOT NULL, " +
                    "salt VARCHAR(100) NOT NULL, " +
                    "email VARCHAR(100), " +
                    "rol VARCHAR(20) NOT NULL" +
                    ")";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        }
    }
    
    /**
     * Yeni bir kullanıcı ekler
     * @return Eklenen kullanıcının ID'si
     */
    public int ekle(Kullanici kullanici) throws SQLException {
        String sql = "INSERT INTO kullanicilar (ad, soyad, kullanici_adi, sifre, salt, email, rol) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            String salt = PasswordUtils.generateSalt();
            String hashedPassword = PasswordUtils.hashPassword(kullanici.getSifre(), salt);
            
            stmt.setString(1, kullanici.getAd());
            stmt.setString(2, kullanici.getSoyad());
            stmt.setString(3, kullanici.getKullaniciAdi());
            stmt.setString(4, hashedPassword);
            stmt.setString(5, salt);
            stmt.setString(6, kullanici.getEmail());
            stmt.setString(7, kullanici.getRol().toString());
            
            stmt.executeUpdate();
            
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int id = generatedKeys.getInt(1);
                    kullanici.setId(id);
                    return id;
                }
            }
            return -1;
        }
    }
    
    /**
     * Kullanıcı bilgilerini günceller
     */
    public boolean guncelle(Kullanici kullanici) throws SQLException {
        String sql = "UPDATE kullanicilar SET kullanici_adi = ?, sifre = ?, salt = ?, ad = ?, " +
                "soyad = ?, email = ?, rol = ? WHERE id = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            String salt = PasswordUtils.generateSalt();
            String hashedPassword = PasswordUtils.hashPassword(kullanici.getSifre(), salt);
            
            pstmt.setString(1, kullanici.getKullaniciAdi());
            pstmt.setString(2, hashedPassword);
            pstmt.setString(3, salt);
            pstmt.setString(4, kullanici.getAd());
            pstmt.setString(5, kullanici.getSoyad());
            pstmt.setString(6, kullanici.getEmail());
            pstmt.setString(7, kullanici.getRol().name());
            pstmt.setInt(8, kullanici.getId());
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        }
    }
    
    /**
     * Kullanıcıyı ID'ye göre siler
     */
    public boolean sil(int id) throws SQLException {
        String sql = "DELETE FROM kullanicilar WHERE id = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        }
    }
    
    /**
     * Tek bir kullanıcıyı ID'ye göre getirir
     */
    public Kullanici getirById(int id) throws SQLException {
        String sql = "SELECT * FROM kullanicilar WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return resultSetToKullanici(rs);
                }
            }
        }
        return null;
    }
    
    /**
     * Kullanıcı adı ve şifreye göre kullanıcı getirir (giriş işlemi için)
     */
    public Kullanici girisYap(String kullaniciAdi, String sifre) throws SQLException {
        String sql = "SELECT * FROM kullanicilar WHERE kullanici_adi = ?";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, kullaniciAdi);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String hashedPassword = rs.getString("sifre");
                    String salt = rs.getString("salt");
                    
                    if (PasswordUtils.verifyPassword(sifre, salt, hashedPassword)) {
                        return resultSetToKullanici(rs);
                    }
                }
            }
        }
        return null;
    }
    
    /**
     * Tüm kullanıcıları getirir
     */
    public List<Kullanici> tumKullanicilariGetir() throws SQLException {
        String sql = "SELECT * FROM kullanicilar ORDER BY ad, soyad";
        List<Kullanici> kullanicilar = new ArrayList<>();
        
        try (Connection conn = dbConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                kullanicilar.add(resultSetToKullanici(rs));
            }
        }
        
        return kullanicilar;
    }
    
    /**
     * Belirli role sahip tüm kullanıcıları getirir
     */
    public List<Kullanici> getRoleGoreKullanicilar(Kullanici.Rol rol) throws SQLException {
        String sql = "SELECT * FROM kullanicilar WHERE rol = ? ORDER BY ad, soyad";
        List<Kullanici> kullanicilar = new ArrayList<>();
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, rol.name());
            
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                kullanicilar.add(resultSetToKullanici(rs));
            }
        }
        
        return kullanicilar;
    }
    
    /**
     * Belirli bir kullanıcı adına sahip kullanıcının var olup olmadığını kontrol eder
     */
    public boolean kullaniciVarMi(String kullaniciAdi) throws SQLException {
        String sql = "SELECT COUNT(*) FROM kullanicilar WHERE kullanici_adi = ?";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, kullaniciAdi);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }
    
    /**
     * ResultSet'ten Kullanici nesnesine dönüşüm
     */
    private Kullanici resultSetToKullanici(ResultSet rs) throws SQLException {
        Kullanici kullanici = new Kullanici();
        kullanici.setId(rs.getInt("id"));
        kullanici.setAd(rs.getString("ad"));
        kullanici.setSoyad(rs.getString("soyad"));
        kullanici.setKullaniciAdi(rs.getString("kullanici_adi"));
        kullanici.setSifre(rs.getString("sifre"));
        kullanici.setSalt(rs.getString("salt"));
        kullanici.setEmail(rs.getString("email"));
        kullanici.setRol(Kullanici.Rol.valueOf(rs.getString("rol")));
        return kullanici;
    }
}
