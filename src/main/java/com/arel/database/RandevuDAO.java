package com.arel.database;

import com.arel.model.Randevu;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Randevu verilerini veritabanında saklayan ve yöneten DAO sınıfı
 */
public class RandevuDAO {
    private final DatabaseConnection dbConnection;
    private final KullaniciDAO kullaniciDAO;
    
    public RandevuDAO() {
        this.dbConnection = DatabaseConnection.getInstance();
        this.kullaniciDAO = new KullaniciDAO();
    }
    
    /**
     * Randevu tablosunu oluşturur
     */
    public void createTable() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS randevular (" +
                "id SERIAL PRIMARY KEY," +
                "ogrenci_id INTEGER NOT NULL REFERENCES kullanicilar(id) ON DELETE CASCADE," +
                "ogretim_uyesi_id INTEGER NOT NULL REFERENCES kullanicilar(id) ON DELETE CASCADE," +
                "baslangic_zamani TIMESTAMP NOT NULL," +
                "bitis_zamani TIMESTAMP NOT NULL," +
                "konu VARCHAR(200)," +
                "notlar TEXT," +
                "durum VARCHAR(20) NOT NULL," +
                "olusturulma_tarihi TIMESTAMP NOT NULL" +
                ")";
        
        try (Connection conn = dbConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            System.out.println("Randevular tablosu oluşturuldu");
        }
    }
    
    /**
     * Yeni randevu ekler
     */
    public int ekle(Randevu randevu) throws SQLException {
        String sql = "INSERT INTO randevular (ogrenci_id, ogretim_uyesi_id, baslangic_zamani, " +
                "bitis_zamani, konu, notlar, durum, olusturulma_tarihi) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?) RETURNING id";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, randevu.getOgrenciId());
            pstmt.setInt(2, randevu.getOgretimUyesiId());
            pstmt.setTimestamp(3, Timestamp.valueOf(randevu.getBaslangicZamani()));
            pstmt.setTimestamp(4, Timestamp.valueOf(randevu.getBitisZamani()));
            pstmt.setString(5, randevu.getKonu());
            pstmt.setString(6, randevu.getNotlar());
            pstmt.setString(7, randevu.getDurum().name());
            pstmt.setTimestamp(8, Timestamp.valueOf(randevu.getOlusturulmaTarihi()));
            
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                int id = rs.getInt("id");
                randevu.setId(id);
                return id;
            }
            return -1;
        }
    }
    
    /**
     * Randevu bilgilerini günceller
     */
    public boolean guncelle(Randevu randevu) throws SQLException {
        String sql = "UPDATE randevular SET ogrenci_id = ?, ogretim_uyesi_id = ?, " +
                "baslangic_zamani = ?, bitis_zamani = ?, konu = ?, notlar = ?, durum = ? " +
                "WHERE id = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, randevu.getOgrenciId());
            pstmt.setInt(2, randevu.getOgretimUyesiId());
            pstmt.setTimestamp(3, Timestamp.valueOf(randevu.getBaslangicZamani()));
            pstmt.setTimestamp(4, Timestamp.valueOf(randevu.getBitisZamani()));
            pstmt.setString(5, randevu.getKonu());
            pstmt.setString(6, randevu.getNotlar());
            pstmt.setString(7, randevu.getDurum().name());
            pstmt.setInt(8, randevu.getId());
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        }
    }
    
    /**
     * Randevu durumunu günceller
     */
    public boolean durumGuncelle(int randevuId, Randevu.Durum durum) throws SQLException {
        String sql = "UPDATE randevular SET durum = ? WHERE id = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, durum.name());
            pstmt.setInt(2, randevuId);
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        }
    }
    
    /**
     * Randevuyu siler
     */
    public boolean sil(int id) throws SQLException {
        String sql = "DELETE FROM randevular WHERE id = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        }
    }
    
    /**
     * ID'ye göre randevu getirir
     */
    public Randevu getirById(int id) throws SQLException {
        String sql = "SELECT * FROM randevular WHERE id = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                Randevu randevu = resultSetToRandevu(rs);
                // İlişkili öğrenci ve öğretim üyesi bilgilerini yükle
                randevu.setOgrenci(kullaniciDAO.getirById(randevu.getOgrenciId()));
                randevu.setOgretimUyesi(kullaniciDAO.getirById(randevu.getOgretimUyesiId()));
                return randevu;
            }
            return null;
        }
    }
    
    /**
     * Öğrencinin randevularını getirir
     */
    public List<Randevu> ogrencininRandevulariniGetir(int ogrenciId) throws SQLException {
        return kullanicininRandevulariniGetir("ogrenci_id", ogrenciId);
    }
    
    /**
     * Öğretim üyesinin randevularını getirir
     */
    public List<Randevu> ogretimUyesininRandevulariniGetir(int ogretimUyesiId) throws SQLException {
        return kullanicininRandevulariniGetir("ogretim_uyesi_id", ogretimUyesiId);
    }
    
    /**
     * Belirli bir kullanıcının randevularını getirir (öğrenci veya öğretim üyesi)
     */
    private List<Randevu> kullanicininRandevulariniGetir(String kolonAdi, int kullaniciId) throws SQLException {
        String sql = "SELECT * FROM randevular WHERE " + kolonAdi + " = ? ORDER BY baslangic_zamani";
        List<Randevu> randevular = new ArrayList<>();
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, kullaniciId);
            
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Randevu randevu = resultSetToRandevu(rs);
                // İlişkili öğrenci ve öğretim üyesi bilgilerini yükle
                randevu.setOgrenci(kullaniciDAO.getirById(randevu.getOgrenciId()));
                randevu.setOgretimUyesi(kullaniciDAO.getirById(randevu.getOgretimUyesiId()));
                randevular.add(randevu);
            }
        }
        
        return randevular;
    }
    
    /**
     * Belirli bir gündeki randevuları getirir
     */
    public List<Randevu> gunlukRandevulariGetir(LocalDate gun, int ogretimUyesiId) throws SQLException {
        String sql = "SELECT * FROM randevular WHERE ogretim_uyesi_id = ? " +
                "AND date_trunc('day', baslangic_zamani) = ? " +
                "ORDER BY baslangic_zamani";
        List<Randevu> randevular = new ArrayList<>();
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, ogretimUyesiId);
            pstmt.setTimestamp(2, Timestamp.valueOf(gun.atStartOfDay()));
            
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Randevu randevu = resultSetToRandevu(rs);
                // İlişkili öğrenci ve öğretim üyesi bilgilerini yükle
                randevu.setOgrenci(kullaniciDAO.getirById(randevu.getOgrenciId()));
                randevu.setOgretimUyesi(kullaniciDAO.getirById(randevu.getOgretimUyesiId()));
                randevular.add(randevu);
            }
        }
        
        return randevular;
    }
    
    /**
     * Belirli zaman aralığında çakışan randevuları kontrol eder
     */
    public boolean zamanCakismasiVarMi(LocalDateTime baslangic, LocalDateTime bitis, int ogretimUyesiId, Integer haricRandevuId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM randevular " +
                "WHERE ogretim_uyesi_id = ? " +
                "AND durum NOT IN ('REDDEDILDI', 'IPTAL_EDILDI') " +
                "AND ((baslangic_zamani <= ? AND bitis_zamani > ?) " +
                "OR (baslangic_zamani < ? AND bitis_zamani >= ?)) " +
                (haricRandevuId != null ? "AND id != ? " : "");
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, ogretimUyesiId);
            pstmt.setTimestamp(2, Timestamp.valueOf(bitis));
            pstmt.setTimestamp(3, Timestamp.valueOf(baslangic));
            pstmt.setTimestamp(4, Timestamp.valueOf(bitis));
            pstmt.setTimestamp(5, Timestamp.valueOf(baslangic));
            
            if (haricRandevuId != null) {
                pstmt.setInt(6, haricRandevuId);
            }
            
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            return false;
        }
    }
    
    /**
     * ResultSet'ten Randevu nesnesine dönüşüm
     */
    private Randevu resultSetToRandevu(ResultSet rs) throws SQLException {
        Randevu randevu = new Randevu();
        randevu.setId(rs.getInt("id"));
        randevu.setOgrenciId(rs.getInt("ogrenci_id"));
        randevu.setOgretimUyesiId(rs.getInt("ogretim_uyesi_id"));
        randevu.setBaslangicZamani(rs.getTimestamp("baslangic_zamani").toLocalDateTime());
        randevu.setBitisZamani(rs.getTimestamp("bitis_zamani").toLocalDateTime());
        randevu.setKonu(rs.getString("konu"));
        randevu.setNotlar(rs.getString("notlar"));
        randevu.setDurum(Randevu.Durum.valueOf(rs.getString("durum")));
        randevu.setOlusturulmaTarihi(rs.getTimestamp("olusturulma_tarihi").toLocalDateTime());
        return randevu;
    }
}
