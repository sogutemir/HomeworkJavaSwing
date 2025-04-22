package com.arel;

import com.arel.database.BildirimDAO;
import com.arel.database.DatabaseConnection;
import com.arel.database.KullaniciDAO;
import com.arel.database.MusaitlikDAO;
import com.arel.database.RandevuDAO;
import com.arel.model.Bildirim;
import com.arel.model.Kullanici;
import com.arel.model.Musaitlik;
import com.arel.model.Randevu;
import com.arel.view.LoginFrame;
import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;
import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Akademik Randevu ve Takip Sistemi uygulamasını başlatan ana sınıf
 */
public class Main {
    public static void main(String[] args) {
        try {
            // Modern görünüm temasını ayarla
            FlatLightLaf.setup();
            UIManager.setLookAndFeel(new FlatLightLaf());
            
            // Veritabanı tablolarını oluştur
            createDatabaseTables();
            
            // Giriş ekranını başlat
            SwingUtilities.invokeLater(() -> {
                LoginFrame loginFrame = new LoginFrame();
                loginFrame.setVisible(true);
            });
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, 
                "Uygulama başlatılırken bir hata oluştu: " + e.getMessage(), 
                "Hata", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    
    /**
     * Veritabanı tablolarını oluşturur
     */
    private static void createDatabaseTables() throws SQLException {
        KullaniciDAO kullaniciDAO = new KullaniciDAO();
        RandevuDAO randevuDAO = new RandevuDAO();
        MusaitlikDAO musaitlikDAO = new MusaitlikDAO();
        BildirimDAO bildirimDAO = new BildirimDAO();
        
        kullaniciDAO.createTable();
        randevuDAO.createTable();
        musaitlikDAO.createTable();
        bildirimDAO.createTable();
        
        // Test verilerini ekle
        createTestData(kullaniciDAO, randevuDAO, musaitlikDAO, bildirimDAO);
    }

    /**
     * Test amaçlı örnek verileri ekler
     */
    private static void createTestData(KullaniciDAO kullaniciDAO, RandevuDAO randevuDAO, 
                                     MusaitlikDAO musaitlikDAO, BildirimDAO bildirimDAO) throws SQLException {
        // Test verilerinin daha önce eklenip eklenmediğini kontrol et
        if (kullaniciDAO.kullaniciVarMi("admin")) {
            System.out.println("Test verileri zaten mevcut, tekrar eklenmeyecek.");
            return;
        }
        
        // Admin kullanıcısı
        Kullanici admin = new Kullanici();
        admin.setKullaniciAdi("admin");
        admin.setSifre("admin123");
        admin.setAd("Admin");
        admin.setSoyad("User");
        admin.setEmail("admin@example.com");
        admin.setRol(Kullanici.Rol.ADMIN);
        kullaniciDAO.ekle(admin);

        // Öğretim üyesi
        Kullanici ogretimUyesi = new Kullanici();
        ogretimUyesi.setKullaniciAdi("ogretim1");
        ogretimUyesi.setSifre("ogretim123");
        ogretimUyesi.setAd("Ahmet");
        ogretimUyesi.setSoyad("Yılmaz");
        ogretimUyesi.setEmail("ahmet.yilmaz@example.com");
        ogretimUyesi.setRol(Kullanici.Rol.OGRETIM_UYESI);
        int ogretimUyesiId = kullaniciDAO.ekle(ogretimUyesi);

        // Öğrenci
        Kullanici ogrenci = new Kullanici();
        ogrenci.setKullaniciAdi("ogrenci1");
        ogrenci.setSifre("ogrenci123");
        ogrenci.setAd("Ayşe");
        ogrenci.setSoyad("Kaya");
        ogrenci.setEmail("ayse.kaya@example.com");
        ogrenci.setRol(Kullanici.Rol.OGRENCI);
        int ogrenciId = kullaniciDAO.ekle(ogrenci);

        // Pazartesi müsaitliği
        Musaitlik pazartesiMusaitlik = new Musaitlik();
        pazartesiMusaitlik.setOgretimUyesiId(ogretimUyesiId);
        pazartesiMusaitlik.setGun(DayOfWeek.MONDAY);
        pazartesiMusaitlik.setBaslangicSaati(LocalTime.of(9, 0));
        pazartesiMusaitlik.setBitisSaati(LocalTime.of(12, 0));
        pazartesiMusaitlik.setTekrarEden(true);
        musaitlikDAO.ekle(pazartesiMusaitlik);

        // Örnek randevu
        Randevu randevu = new Randevu();
        randevu.setOgrenciId(ogrenciId);
        randevu.setOgretimUyesiId(ogretimUyesiId);
        randevu.setBaslangicZamani(LocalDateTime.now().plusDays(1).withHour(10).withMinute(0));
        randevu.setBitisZamani(LocalDateTime.now().plusDays(1).withHour(11).withMinute(0));
        randevu.setKonu("Proje Görüşmesi");
        randevu.setNotlar("Bitirme projesi hakkında görüşme");
        randevu.setDurum(Randevu.Durum.BEKLEMEDE);
        int randevuId = randevuDAO.ekle(randevu);

        // Örnek bildirim
        Bildirim bildirim = new Bildirim();
        bildirim.setKullaniciId(ogrenciId);
        bildirim.setRandevuId(randevuId);
        bildirim.setBaslik("Yeni Randevu Talebi");
        bildirim.setMesaj("Randevu talebiniz alınmıştır. Öğretim üyesinin onayı bekleniyor.");
        bildirim.setTip(Bildirim.Tip.RANDEVU_OLUSTURULDU);
        bildirimDAO.ekle(bildirim);
    }

    /**
     * Uygulama kapanırken veritabanı bağlantısını kapatır
     */
    public static void shutdown() {
        try {
            DatabaseConnection.getInstance().closeConnection();
            System.out.println("Uygulama düzgün bir şekilde kapatıldı.");
        } catch (Exception e) {
            System.err.println("Uygulama kapatılırken hata: " + e.getMessage());
        }
    }
}