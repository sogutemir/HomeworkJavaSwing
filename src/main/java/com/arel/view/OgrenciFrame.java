package com.arel.view;

import com.arel.database.KullaniciDAO;
import com.arel.database.RandevuDAO;
import com.arel.model.Kullanici;
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
 * Öğrenci kullanıcı arayüzü
 */
public class OgrenciFrame extends JFrame {
    
    private Kullanici ogrenci;
    private RandevuDAO randevuDAO;
    private KullaniciDAO kullaniciDAO;
    
    private JTable tblRandevular;
    private DefaultTableModel tableModel;
    private JButton btnYeniRandevu;
    private JButton btnRandevuIptal;
    private JButton btnYenile;
    private JButton btnCikis;
    
    public OgrenciFrame(Kullanici ogrenci) {
        this.ogrenci = ogrenci;
        this.randevuDAO = new RandevuDAO();
        this.kullaniciDAO = new KullaniciDAO();
        
        initComponents();
        setupListeners();
        randevulariYukle();
    }
    
    private void initComponents() {
        setTitle("Akademik Randevu Sistemi - Öğrenci: " + ogrenci.getTamAd());
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        // Ana panel
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Üst Panel - Hoş geldiniz mesajı
        JPanel ustPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel lblHosgeldiniz = new JLabel("Hoş geldiniz, " + ogrenci.getTamAd());
        lblHosgeldiniz.setFont(new Font("Arial", Font.BOLD, 16));
        ustPanel.add(lblHosgeldiniz);
        
        // Tablo - Randevular
        String[] kolonlar = {"ID", "Öğretim Üyesi", "Tarih", "Saat", "Konu", "Durum"};
        tableModel = new DefaultTableModel(kolonlar, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tblRandevular = new JTable(tableModel);
        tblRandevular.getTableHeader().setReorderingAllowed(false);
        tblRandevular.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        JScrollPane scrollPane = new JScrollPane(tblRandevular);
        scrollPane.setPreferredSize(new Dimension(750, 400));
        
        // Butonlar Paneli
        JPanel butonlarPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        
        btnYeniRandevu = new JButton("Yeni Randevu Oluştur");
        btnRandevuIptal = new JButton("Seçili Randevuyu İptal Et");
        btnYenile = new JButton("Yenile");
        btnCikis = new JButton("Çıkış");
        
        butonlarPanel.add(btnYeniRandevu);
        butonlarPanel.add(btnRandevuIptal);
        butonlarPanel.add(btnYenile);
        butonlarPanel.add(btnCikis);
        
        // Panelleri ana panele ekle
        panel.add(ustPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(butonlarPanel, BorderLayout.SOUTH);
        
        getContentPane().add(panel);
    }
    
    private void setupListeners() {
        btnYeniRandevu.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                yeniRandevuOlustur();
            }
        });
        
        btnRandevuIptal.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                secilenRandevuyuIptalEt();
            }
        });
        
        btnYenile.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                randevulariYukle();
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
     * Öğrencinin randevularını yükler
     */
    private void randevulariYukle() {
        try {
            // Tabloyu temizle
            tableModel.setRowCount(0);
            
            // Randevuları getir
            List<Randevu> randevular = randevuDAO.ogrencininRandevulariniGetir(ogrenci.getId());
            
            // Tabloyu doldur
            for (Randevu randevu : randevular) {
                Kullanici ogretimUyesi = randevu.getOgretimUyesi();
                String ogretimUyesiAdi = ogretimUyesi != null ? ogretimUyesi.getTamAd() : "";
                
                Object[] satir = {
                    randevu.getId(),
                    ogretimUyesiAdi,
                    DateTimeUtil.formatDate(randevu.getBaslangicZamani().toLocalDate()),
                    DateTimeUtil.formatTime(randevu.getBaslangicZamani().toLocalTime()) + " - " + 
                    DateTimeUtil.formatTime(randevu.getBitisZamani().toLocalTime()),
                    randevu.getKonu(),
                    randevu.getDurum().toString()
                };
                
                tableModel.addRow(satir);
            }
            
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, 
                "Randevular yüklenirken hata oluştu: " + ex.getMessage(), 
                "Hata", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }
    
    /**
     * Yeni randevu oluşturma ekranını açar
     */
    private void yeniRandevuOlustur() {
        RandevuOlusturFrame randevuFrame = new RandevuOlusturFrame(ogrenci);
        randevuFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                randevulariYukle();
            }
        });
        randevuFrame.setVisible(true);
    }
    
    /**
     * Seçilen randevuyu iptal eder
     */
    private void secilenRandevuyuIptalEt() {
        int selectedRow = tblRandevular.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, 
                "Lütfen iptal etmek istediğiniz randevuyu seçin.", 
                "Uyarı", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int randevuId = (int) tableModel.getValueAt(selectedRow, 0);
        String durum = (String) tableModel.getValueAt(selectedRow, 5);
        
        // İptal edilebilir mi kontrol et
        if (durum.equals(Randevu.Durum.TAMAMLANDI.toString()) || 
            durum.equals(Randevu.Durum.IPTAL_EDILDI.toString())) {
            JOptionPane.showMessageDialog(this, 
                "Bu randevu iptal edilemez.", 
                "Uyarı", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Onay iste
        int secim = JOptionPane.showConfirmDialog(this, 
            "Seçili randevuyu iptal etmek istediğinize emin misiniz?", 
            "Randevu İptal", JOptionPane.YES_NO_OPTION);
        
        if (secim == JOptionPane.YES_OPTION) {
            try {
                // Randevu durumunu güncelle
                boolean sonuc = randevuDAO.durumGuncelle(randevuId, Randevu.Durum.IPTAL_EDILDI);
                
                if (sonuc) {
                    JOptionPane.showMessageDialog(this, 
                        "Randevu başarıyla iptal edildi.", 
                        "İşlem Başarılı", JOptionPane.INFORMATION_MESSAGE);
                    
                    // Tabloyu yenile
                    randevulariYukle();
                } else {
                    JOptionPane.showMessageDialog(this, 
                        "Randevu iptal edilirken bir hata oluştu.", 
                        "Hata", JOptionPane.ERROR_MESSAGE);
                }
                
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, 
                    "Randevu iptal edilirken hata oluştu: " + ex.getMessage(), 
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