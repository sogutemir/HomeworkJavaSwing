package com.arel.database;

import com.arel.model.Musaitlik;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.List;

/**
 * Öğretim üyelerinin müsaitlik bilgilerini veritabanında saklayan ve yöneten DAO sınıfı
 */
public class MusaitlikDAO {
    private final DatabaseConnection dbConnection;
    private final KullaniciDAO kullaniciDAO;
    
    public MusaitlikDAO() {
        this.dbConnection = DatabaseConnection.getInstance();
        this.kullaniciDAO = new KullaniciDAO();
    }
    
    /**
     * Müsaitlik tablosunu oluşturur
     */
    public void createTable() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS musaitlikler (" +
                "id SERIAL PRIMARY KEY," +
                "ogretim_uyesi_id INTEGER NOT NULL REFERENCES kullanicilar(id) ON DELETE CASCADE," +
                "gun INTEGER," +
                "tarih DATE," +
                "baslangic_saati TIME NOT NULL," +
                "bitis_saati TIME NOT NULL," +
                "tekrar_eden BOOLEAN NOT NULL DEFAULT false," +
                "UNIQUE(ogretim_uyesi_id, gun, baslangic_saati, bitis_saati)," +
                "UNIQUE(ogretim_uyesi_id, tarih, baslangic_saati, bitis_saati)" +
                ")";
        
        try (Connection conn = dbConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            System.out.println("Müsaitlikler tablosu oluşturuldu");
        }
    }
    
    /**
     * Yeni müsaitlik ekler
     */
    public int ekle(Musaitlik musaitlik) throws SQLException {
        String sql = "INSERT INTO musaitlikler (ogretim_uyesi_id, gun, tarih, " +
                "baslangic_saati, bitis_saati, tekrar_eden) VALUES (?, ?, ?, ?, ?, ?) RETURNING id";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, musaitlik.getOgretimUyesiId());
            
            if (musaitlik.isTekrarEden()) {
                pstmt.setInt(2, musaitlik.getGun().getValue());
                pstmt.setNull(3, java.sql.Types.DATE);
            } else {
                pstmt.setNull(2, java.sql.Types.INTEGER);
                if (musaitlik.getTarih() != null) {
                    pstmt.setDate(3, java.sql.Date.valueOf(musaitlik.getTarih()));
                } else {
                    pstmt.setNull(3, java.sql.Types.DATE);
                }
            }
            
            pstmt.setTime(4, Time.valueOf(musaitlik.getBaslangicSaati()));
            pstmt.setTime(5, Time.valueOf(musaitlik.getBitisSaati()));
            pstmt.setBoolean(6, musaitlik.isTekrarEden());
            
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                int id = rs.getInt("id");
                musaitlik.setId(id);
                return id;
            }
            return -1;
        }
    }
    
    /**
     * Müsaitlik bilgilerini günceller
     */
    public boolean guncelle(Musaitlik musaitlik) throws SQLException {
        String sql = "UPDATE musaitlikler SET ogretim_uyesi_id = ?, gun = ?, " +
                "baslangic_saati = ?, bitis_saati = ? WHERE id = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, musaitlik.getOgretimUyesiId());
            pstmt.setInt(2, musaitlik.getGun().getValue());
            pstmt.setTime(3, Time.valueOf(musaitlik.getBaslangicSaati()));
            pstmt.setTime(4, Time.valueOf(musaitlik.getBitisSaati()));
            pstmt.setInt(5, musaitlik.getId());
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        }
    }
    
    /**
     * Müsaitlik kaydını siler
     */
    public boolean sil(int id) throws SQLException {
        String sql = "DELETE FROM musaitlikler WHERE id = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        }
    }
    
    /**
     * Öğretim üyesinin tüm müsaitlik bilgilerini siler
     */
    public boolean ogretimUyesiMusaitlikleriniSil(int ogretimUyesiId) throws SQLException {
        String sql = "DELETE FROM musaitlikler WHERE ogretim_uyesi_id = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, ogretimUyesiId);
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        }
    }
    
    /**
     * ID'ye göre tek bir müsaitlik kaydını getirir
     */
    public Musaitlik getirById(int id) throws SQLException {
        String sql = "SELECT * FROM musaitlikler WHERE id = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                Musaitlik musaitlik = resultSetToMusaitlik(rs);
                // İlişkili öğretim üyesi bilgisini yükle
                musaitlik.setOgretimUyesi(kullaniciDAO.getirById(musaitlik.getOgretimUyesiId()));
                return musaitlik;
            }
            return null;
        }
    }
    
    /**
     * Öğretim üyesinin müsaitlik zamanlarını getirir
     */
    public List<Musaitlik> ogretimUyesininMusaitlikleriniGetir(int ogretimUyesiId) throws SQLException {
        String sql = "SELECT * FROM musaitlikler WHERE ogretim_uyesi_id = ? " +
                "ORDER BY gun, baslangic_saati";
        List<Musaitlik> musaitlikler = new ArrayList<>();
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, ogretimUyesiId);
            
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Musaitlik musaitlik = resultSetToMusaitlik(rs);
                musaitlik.setOgretimUyesi(kullaniciDAO.getirById(ogretimUyesiId));
                musaitlikler.add(musaitlik);
            }
        }
        
        return musaitlikler;
    }
    
    /**
     * Belirli bir gün için öğretim üyelerinin müsaitlik zamanlarını getirir
     */
    public List<Musaitlik> gunlukMusaitlikleriGetir(DayOfWeek gun, int ogretimUyesiId) throws SQLException {
        String sql = "SELECT * FROM musaitlikler WHERE ogretim_uyesi_id = ? AND gun = ? " +
                "ORDER BY baslangic_saati";
        List<Musaitlik> musaitlikler = new ArrayList<>();
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, ogretimUyesiId);
            pstmt.setInt(2, gun.getValue());
            
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Musaitlik musaitlik = resultSetToMusaitlik(rs);
                musaitlik.setOgretimUyesi(kullaniciDAO.getirById(ogretimUyesiId));
                musaitlikler.add(musaitlik);
            }
        }
        
        return musaitlikler;
    }
    
    /**
     * ResultSet'ten Musaitlik nesnesine dönüşüm
     */
    private Musaitlik resultSetToMusaitlik(ResultSet rs) throws SQLException {
        Musaitlik musaitlik = new Musaitlik();
        musaitlik.setId(rs.getInt("id"));
        musaitlik.setOgretimUyesiId(rs.getInt("ogretim_uyesi_id"));
        
        int haftaninGunu = rs.getInt("gun");
        if (!rs.wasNull()) {
            musaitlik.setGun(DayOfWeek.of(haftaninGunu));
        }
        
        java.sql.Date tarih = rs.getDate("tarih");
        if (tarih != null) {
            musaitlik.setTarih(tarih.toLocalDate());
        }
        
        musaitlik.setBaslangicSaati(rs.getTime("baslangic_saati").toLocalTime());
        musaitlik.setBitisSaati(rs.getTime("bitis_saati").toLocalTime());
        musaitlik.setTekrarEden(rs.getBoolean("tekrar_eden"));
        return musaitlik;
    }
}
