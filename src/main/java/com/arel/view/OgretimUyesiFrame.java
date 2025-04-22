package com.arel.view;

import com.arel.database.KullaniciDAO;
import com.arel.database.MusaitlikDAO;
import com.arel.database.RandevuDAO;
import com.arel.model.Kullanici;
import com.arel.model.Musaitlik;
import com.arel.model.Randevu;
import com.arel.util.DateTimeUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.SQLException;
import java.util.List;

/**
 * Öğretim üyesi kullanıcı arayüzü
 */
public class OgretimUyesiFrame extends JFrame {
    
    private Kullanici ogretimUyesi;
    private RandevuDAO randevuDAO;
    private MusaitlikDAO musaitlikDAO;
    private KullaniciDAO kullaniciDAO;
    
    private JTabbedPane tabbedPane;
    private JTable tblRandevular;
    private JTable tblMusaitlikler;
    private DefaultTableModel randevuTableModel;
    private DefaultTableModel musaitlikTableModel;
    
    private JButton btnRandevuOnayla;
    private JButton btnRandevuReddet;
    private JButton btnMusaitlikEkle;
    private JButton btnMusaitlikSil;
    private JButton btnYenile;
    private JButton btnCikis;
    
    public OgretimUyesiFrame(Kullanici ogretimUyesi) {
        this.ogretimUyesi = ogretimUyesi;
        this.randevuDAO = new RandevuDAO();
        this.musaitlikDAO = new MusaitlikDAO();
        this.kullaniciDAO = new KullaniciDAO();
        
        initComponents();
        setupListeners();
        verileriYukle();
    }
    
    private void initComponents() {
        setTitle("Akademik Randevu Sistemi - Öğretim Üyesi: " + ogretimUyesi.getTamAd());
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        // Sekmeli panel
        tabbedPane = new JTabbedPane();
        
        // Randevular Sekmesi
        JPanel randevularPanel = createRandevularPanel();
        tabbedPane.addTab("Randevular", randevularPanel);
        
        // Müsaitlik Zamanları Sekmesi
        JPanel musaitliklerPanel = createMusaitliklerPanel();
        tabbedPane.addTab("Müsaitlik Zamanları", musaitliklerPanel);
        
        // Ana pencereye ekle
        getContentPane().add(tabbedPane);
    }
    
    private JPanel createRandevularPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Üst Panel - Hoş geldiniz mesajı
        JPanel ustPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel lblHosgeldiniz = new JLabel("Hoş geldiniz, " + ogretimUyesi.getTamAd());
        lblHosgeldiniz.setFont(new Font("Arial", Font.BOLD, 16));
        ustPanel.add(lblHosgeldiniz);
        
        // Tablo - Randevular
        String[] kolonlar = {"ID", "Öğrenci", "Tarih", "Saat", "Konu", "Durum"};
        randevuTableModel = new DefaultTableModel(kolonlar, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tblRandevular = new JTable(randevuTableModel);
        tblRandevular.getTableHeader().setReorderingAllowed(false);
        tblRandevular.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        JScrollPane scrollPane = new JScrollPane(tblRandevular);
        scrollPane.setPreferredSize(new Dimension(850, 400));
        
        // Butonlar Paneli
        JPanel butonlarPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        
        btnRandevuOnayla = new JButton("Seçili Randevuyu Onayla");
        btnRandevuReddet = new JButton("Seçili Randevuyu Reddet");
        btnYenile = new JButton("Yenile");
        btnCikis = new JButton("Çıkış");
        
        butonlarPanel.add(btnRandevuOnayla);
        butonlarPanel.add(btnRandevuReddet);
        butonlarPanel.add(btnYenile);
        butonlarPanel.add(btnCikis);
        
        // Panelleri ana panele ekle
        panel.add(ustPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(butonlarPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createMusaitliklerPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Tablo - Müsaitlikler
        String[] kolonlar = {"ID", "Gün", "Başlangıç Saati", "Bitiş Saati"};
        musaitlikTableModel = new DefaultTableModel(kolonlar, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tblMusaitlikler = new JTable(musaitlikTableModel);
        tblMusaitlikler.getTableHeader().setReorderingAllowed(false);
        tblMusaitlikler.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        JScrollPane scrollPane = new JScrollPane(tblMusaitlikler);
        scrollPane.setPreferredSize(new Dimension(850, 400));
        
        // Butonlar Paneli
        JPanel butonlarPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        
        btnMusaitlikEkle = new JButton("Yeni Müsaitlik Ekle");
        btnMusaitlikSil = new JButton("Seçili Müsaitliği Sil");
        
        butonlarPanel.add(btnMusaitlikEkle);
        butonlarPanel.add(btnMusaitlikSil);
        
        // Panelleri ana panele ekle
        panel.add(new JLabel("Müsaitlik Zamanlarınız:"), BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(butonlarPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private void setupListeners() {
        btnRandevuOnayla.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                secilenRandevuyuOnayla();
            }
        });
        
        btnRandevuReddet.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                secilenRandevuyuReddet();
            }
        });
        
        btnMusaitlikEkle.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                yeniMusaitlikEkle();
            }
        });
        
        btnMusaitlikSil.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                secilenMusaitligiSil();
            }
        });
        
        btnYenile.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                verileriYukle();
            }
        });
        
        btnCikis.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cikisYap();
            }
        });
        
        // Pencere kapatılırken veritabanı bağlantısını kapat
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                com.arel.Main.shutdown();
            }
        });
    }
    
    /**
     * Randevuları ve müsaitlik zamanlarını yükler
     */
    private void verileriYukle() {
        randevulariYukle();
        musaitlikleriYukle();
    }
    
    /**
     * Öğretim üyesinin randevularını yükler
     */
    private void randevulariYukle() {
        try {
            // Tabloyu temizle
            randevuTableModel.setRowCount(0);
            
            // Randevuları getir
            List<Randevu> randevular = randevuDAO.ogretimUyesininRandevulariniGetir(ogretimUyesi.getId());
            
            // Tabloyu doldur
            for (Randevu randevu : randevular) {
                Kullanici ogrenci = randevu.getOgrenci();
                String ogrenciAdi = ogrenci != null ? ogrenci.getTamAd() : "";
                
                Object[] satir = {
                    randevu.getId(),
                    ogrenciAdi,
                    DateTimeUtil.formatDate(randevu.getBaslangicZamani().toLocalDate()),
                    DateTimeUtil.formatTime(randevu.getBaslangicZamani().toLocalTime()) + " - " + 
                    DateTimeUtil.formatTime(randevu.getBitisZamani().toLocalTime()),
                    randevu.getKonu(),
                    randevu.getDurum().toString()
                };
                
                randevuTableModel.addRow(satir);
            }
            
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, 
                "Randevular yüklenirken hata oluştu: " + ex.getMessage(), 
                "Hata", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }
    
    /**
     * Öğretim üyesinin müsaitlik zamanlarını yükler
     */
    private void musaitlikleriYukle() {
        try {
            // Tabloyu temizle
            musaitlikTableModel.setRowCount(0);
            
            // Müsaitlikleri getir
            List<Musaitlik> musaitlikler = musaitlikDAO.ogretimUyesininMusaitlikleriniGetir(ogretimUyesi.getId());
            
            // Tabloyu doldur
            for (Musaitlik musaitlik : musaitlikler) {
                Object[] satir = {
                    musaitlik.getId(),
                    musaitlik.getGunAdi(),
                    DateTimeUtil.formatTime(musaitlik.getBaslangicSaati()),
                    DateTimeUtil.formatTime(musaitlik.getBitisSaati())
                };
                
                musaitlikTableModel.addRow(satir);
            }
            
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, 
                "Müsaitlik zamanları yüklenirken hata oluştu: " + ex.getMessage(), 
                "Hata", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }
    
    /**
     * Seçilen randevuyu onaylar
     */
    private void secilenRandevuyuOnayla() {
        int selectedRow = tblRandevular.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, 
                "Lütfen onaylamak istediğiniz randevuyu seçin.", 
                "Uyarı", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int randevuId = (int) randevuTableModel.getValueAt(selectedRow, 0);
        String durum = (String) randevuTableModel.getValueAt(selectedRow, 5);
        
        // Onaylanabilir mi kontrol et
        if (!durum.equals(Randevu.Durum.BEKLEMEDE.toString())) {
            JOptionPane.showMessageDialog(this, 
                "Sadece beklemede olan randevular onaylanabilir.", 
                "Uyarı", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Onay iste
        int secim = JOptionPane.showConfirmDialog(this, 
            "Seçili randevuyu onaylamak istediğinize emin misiniz?", 
            "Randevu Onay", JOptionPane.YES_NO_OPTION);
        
        if (secim == JOptionPane.YES_OPTION) {
            try {
                // Randevu durumunu güncelle
                boolean sonuc = randevuDAO.durumGuncelle(randevuId, Randevu.Durum.ONAYLANDI);
                
                if (sonuc) {
                    JOptionPane.showMessageDialog(this, 
                        "Randevu başarıyla onaylandı.", 
                        "İşlem Başarılı", JOptionPane.INFORMATION_MESSAGE);
                    
                    // Tabloyu yenile
                    randevulariYukle();
                } else {
                    JOptionPane.showMessageDialog(this, 
                        "Randevu onaylanırken bir hata oluştu.", 
                        "Hata", JOptionPane.ERROR_MESSAGE);
                }
                
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, 
                    "Randevu onaylanırken hata oluştu: " + ex.getMessage(), 
                    "Hata", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        }
    }
    
    /**
     * Seçilen randevuyu reddeder
     */
    private void secilenRandevuyuReddet() {
        int selectedRow = tblRandevular.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, 
                "Lütfen reddetmek istediğiniz randevuyu seçin.", 
                "Uyarı", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int randevuId = (int) randevuTableModel.getValueAt(selectedRow, 0);
        String durum = (String) randevuTableModel.getValueAt(selectedRow, 5);
        
        // Reddedilebilir mi kontrol et
        if (!durum.equals(Randevu.Durum.BEKLEMEDE.toString())) {
            JOptionPane.showMessageDialog(this, 
                "Sadece beklemede olan randevular reddedilebilir.", 
                "Uyarı", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Red nedeni sor
        String redNedeni = JOptionPane.showInputDialog(this, 
            "Randevuyu reddetme nedeniniz:", 
            "Randevu Reddetme", JOptionPane.QUESTION_MESSAGE);
        
        if (redNedeni != null && !redNedeni.isEmpty()) {
            try {
                // Randevu durumunu güncelle
                boolean sonuc = randevuDAO.durumGuncelle(randevuId, Randevu.Durum.REDDEDILDI);
                
                if (sonuc) {
                    JOptionPane.showMessageDialog(this, 
                        "Randevu başarıyla reddedildi.", 
                        "İşlem Başarılı", JOptionPane.INFORMATION_MESSAGE);
                    
                    // Tabloyu yenile
                    randevulariYukle();
                } else {
                    JOptionPane.showMessageDialog(this, 
                        "Randevu reddedilirken bir hata oluştu.", 
                        "Hata", JOptionPane.ERROR_MESSAGE);
                }
                
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, 
                    "Randevu reddedilirken hata oluştu: " + ex.getMessage(), 
                    "Hata", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        }
    }
    
    /**
     * Yeni müsaitlik zamanı ekler
     */
    private void yeniMusaitlikEkle() {
        MusaitlikEkleFrame musaitlikEkleFrame = new MusaitlikEkleFrame(ogretimUyesi.getId());
        musaitlikEkleFrame.setVisible(true);
        musaitlikEkleFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                musaitlikleriYukle();
            }
        });
    }
    
    /**
     * Seçilen müsaitlik zamanını siler
     */
    private void secilenMusaitligiSil() {
        int selectedRow = tblMusaitlikler.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, 
                "Lütfen silmek istediğiniz müsaitlik zamanını seçin.", 
                "Uyarı", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int musaitlikId = (int) musaitlikTableModel.getValueAt(selectedRow, 0);
        
        // Onay iste
        int secim = JOptionPane.showConfirmDialog(this, 
            "Seçili müsaitlik zamanını silmek istediğinize emin misiniz?", 
            "Müsaitlik Silme", JOptionPane.YES_NO_OPTION);
        
        if (secim == JOptionPane.YES_OPTION) {
            try {
                // Müsaitliği sil
                boolean sonuc = musaitlikDAO.sil(musaitlikId);
                
                if (sonuc) {
                    JOptionPane.showMessageDialog(this, 
                        "Müsaitlik zamanı başarıyla silindi.", 
                        "İşlem Başarılı", JOptionPane.INFORMATION_MESSAGE);
                    
                    // Tabloyu yenile
                    musaitlikleriYukle();
                } else {
                    JOptionPane.showMessageDialog(this, 
                        "Müsaitlik zamanı silinirken bir hata oluştu.", 
                        "Hata", JOptionPane.ERROR_MESSAGE);
                }
                
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, 
                    "Müsaitlik zamanı silinirken hata oluştu: " + ex.getMessage(), 
                    "Hata", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        }
    }
    
    /**
     * Çıkış yaparak giriş ekranına döner
     */
    private void cikisYap() {
        int secim = JOptionPane.showConfirmDialog(this, 
            "Çıkış yapmak istediğinize emin misiniz?", 
            "Çıkış", JOptionPane.YES_NO_OPTION);
        
        if (secim == JOptionPane.YES_OPTION) {
            dispose();
            new LoginFrame().setVisible(true);
        }
    }
} 