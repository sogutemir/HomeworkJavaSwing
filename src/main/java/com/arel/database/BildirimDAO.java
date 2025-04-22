package com.arel.database;

import com.arel.model.Bildirim;
import com.arel.model.Randevu;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Bildirim verilerini veritabanında saklayan ve yöneten DAO sınıfı
 */
public class BildirimDAO {
    private final DatabaseConnection dbConnection;
    private final KullaniciDAO kullaniciDAO;
    private final RandevuDAO randevuDAO;
    
    public BildirimDAO() {
        this.dbConnection = DatabaseConnection.getInstance();
        this.kullaniciDAO = new KullaniciDAO();
        this.randevuDAO = new RandevuDAO();
    }
    
    /**
     * Bildirim tablosunu oluşturur
     */
    public void createTable() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS bildirimler (" +
                "id SERIAL PRIMARY KEY," +
                "kullanici_id INTEGER NOT NULL REFERENCES kullanicilar(id) ON DELETE CASCADE," +
                "randevu_id INTEGER REFERENCES randevular(id) ON DELETE CASCADE," +
                "baslik VARCHAR(100) NOT NULL," +
                "mesaj TEXT NOT NULL," +
                "tip VARCHAR(50) NOT NULL," +
                "okundu BOOLEAN NOT NULL DEFAULT FALSE," +
                "olusturulma_tarihi TIMESTAMP NOT NULL" +
                ")";
        
        try (Connection conn = dbConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            System.out.println("Bildirimler tablosu oluşturuldu");
        }
    }
    
    /**
     * Yeni bildirim ekler
     */
    public int ekle(Bildirim bildirim) throws SQLException {
        String sql = "INSERT INTO bildirimler (kullanici_id, randevu_id, baslik, mesaj, tip, okundu, olusturulma_tarihi) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?) RETURNING id";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, bildirim.getKullaniciId());
            
            if (bildirim.getRandevuId() > 0) {
                pstmt.setInt(2, bildirim.getRandevuId());
            } else {
                pstmt.setNull(2, java.sql.Types.INTEGER);
            }
            
            pstmt.setString(3, bildirim.getBaslik());
            pstmt.setString(4, bildirim.getMesaj());
            pstmt.setString(5, bildirim.getTip().name());
            pstmt.setBoolean(6, bildirim.isOkundu());
            pstmt.setTimestamp(7, Timestamp.valueOf(bildirim.getOlusturulmaTarihi()));
            
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                int id = rs.getInt("id");
                bildirim.setId(id);
                return id;
            }
            return -1;
        }
    }
    
    /**
     * Bildirim bilgilerini günceller
     */
    public boolean guncelle(Bildirim bildirim) throws SQLException {
        String sql = "UPDATE bildirimler SET kullanici_id = ?, randevu_id = ?, baslik = ?, " +
                "mesaj = ?, tip = ?, okundu = ? WHERE id = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, bildirim.getKullaniciId());
            
            if (bildirim.getRandevuId() > 0) {
                pstmt.setInt(2, bildirim.getRandevuId());
            } else {
                pstmt.setNull(2, java.sql.Types.INTEGER);
            }
            
            pstmt.setString(3, bildirim.getBaslik());
            pstmt.setString(4, bildirim.getMesaj());
            pstmt.setString(5, bildirim.getTip().name());
            pstmt.setBoolean(6, bildirim.isOkundu());
            pstmt.setInt(7, bildirim.getId());
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        }
    }
    
    /**
     * Bildirimi okundu olarak işaretler
     */
    public boolean okunduOlarakIsaretle(int bildirimId) throws SQLException {
        String sql = "UPDATE bildirimler SET okundu = TRUE WHERE id = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, bildirimId);
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        }
    }
    
    /**
     * Kullanıcının tüm bildirimlerini okundu olarak işaretler
     */
    public boolean tumBildirimleriOkunduYap(int kullaniciId) throws SQLException {
        String sql = "UPDATE bildirimler SET okundu = TRUE WHERE kullanici_id = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, kullaniciId);
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        }
    }
    
    /**
     * Bildirimi siler
     */
    public boolean sil(int id) throws SQLException {
        String sql = "DELETE FROM bildirimler WHERE id = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        }
    }
    
    /**
     * ID'ye göre bildirim getirir
     */
    public Bildirim getirById(int id) throws SQLException {
        String sql = "SELECT * FROM bildirimler WHERE id = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                Bildirim bildirim = resultSetToBildirim(rs);
                // İlişkili nesneleri yükle
                bildirim.setKullanici(kullaniciDAO.getirById(bildirim.getKullaniciId()));
                if (bildirim.getRandevuId() > 0) {
                    bildirim.setRandevu(randevuDAO.getirById(bildirim.getRandevuId()));
                }
                return bildirim;
            }
            return null;
        }
    }
    
    /**
     * Kullanıcının bildirimlerini getirir
     */
    public List<Bildirim> kullaniciBildirimleriniGetir(int kullaniciId, boolean sadeceokunmamislar) throws SQLException {
        String sql = "SELECT * FROM bildirimler WHERE kullanici_id = ? " +
                (sadeceokunmamislar ? "AND okundu = FALSE " : "") +
                "ORDER BY olusturulma_tarihi DESC";
        List<Bildirim> bildirimler = new ArrayList<>();
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, kullaniciId);
            
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Bildirim bildirim = resultSetToBildirim(rs);
                // İlişkili kullanıcı bilgilerini yükle
                bildirim.setKullanici(kullaniciDAO.getirById(kullaniciId));
                bildirimler.add(bildirim);
            }
        }
        
        return bildirimler;
    }
    
    /**
     * Randevu için bildirim oluşturur
     */
    public void randevuBildirimiOlustur(Randevu randevu, Bildirim.Tip bildirimTipi, 
                                      int aliciId, String baslik, String mesaj) throws SQLException {
        Bildirim bildirim = new Bildirim();
        bildirim.setKullaniciId(aliciId);
        bildirim.setRandevuId(randevu.getId());
        bildirim.setBaslik(baslik);
        bildirim.setMesaj(mesaj);
        bildirim.setTip(bildirimTipi);
        bildirim.setOkundu(false);
        bildirim.setOlusturulmaTarihi(LocalDateTime.now());
        
        ekle(bildirim);
    }
    
    /**
     * ResultSet'ten Bildirim nesnesine dönüşüm
     */
    private Bildirim resultSetToBildirim(ResultSet rs) throws SQLException {
        Bildirim bildirim = new Bildirim();
        bildirim.setId(rs.getInt("id"));
        bildirim.setKullaniciId(rs.getInt("kullanici_id"));
        
        int randevuId = rs.getInt("randevu_id");
        if (!rs.wasNull()) {
            bildirim.setRandevuId(randevuId);
        }
        
        bildirim.setBaslik(rs.getString("baslik"));
        bildirim.setMesaj(rs.getString("mesaj"));
        bildirim.setTip(Bildirim.Tip.valueOf(rs.getString("tip")));
        bildirim.setOkundu(rs.getBoolean("okundu"));
        bildirim.setOlusturulmaTarihi(rs.getTimestamp("olusturulma_tarihi").toLocalDateTime());
        return bildirim;
    }
}
