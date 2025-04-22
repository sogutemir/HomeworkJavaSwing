package com.arel.view;

import com.arel.database.KullaniciDAO;
import com.arel.database.RandevuDAO;
import com.arel.model.Kullanici;
import com.arel.util.PasswordUtil;

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
 * Admin kullanıcı arayüzü
 */
public class AdminFrame extends JFrame {
    
    private Kullanici admin;
    private KullaniciDAO kullaniciDAO;
    private RandevuDAO randevuDAO;
    
    private JTabbedPane tabbedPane;
    private JTable tblKullanicilar;
    private DefaultTableModel tableModel;
    
    private JButton btnYeniKullanici;
    private JButton btnKullaniciDuzenle;
    private JButton btnKullaniciSil;
    private JButton btnYenile;
    private JButton btnCikis;
    
    private JComboBox<String> cmbKullaniciTipi;
    
    public AdminFrame(Kullanici admin) {
        this.admin = admin;
        this.kullaniciDAO = new KullaniciDAO();
        this.randevuDAO = new RandevuDAO();
        
        initComponents();
        setupListeners();
        kullanicilariYukle();
    }
    
    private void initComponents() {
        setTitle("Akademik Randevu Sistemi - Admin: " + admin.getTamAd());
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        // Ana panel
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Üst Panel - Hoş geldiniz mesajı ve filtre
        JPanel ustPanel = new JPanel(new BorderLayout());
        
        JPanel solUstPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel lblHosgeldiniz = new JLabel("Hoş geldiniz, " + admin.getTamAd());
        lblHosgeldiniz.setFont(new Font("Arial", Font.BOLD, 16));
        solUstPanel.add(lblHosgeldiniz);
        
        JPanel sagUstPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JLabel lblFiltre = new JLabel("Kullanıcı Tipi Filtresi:");
        cmbKullaniciTipi = new JComboBox<>(new String[]{"Tümü", "Öğrenci", "Öğretim Üyesi", "Admin"});
        cmbKullaniciTipi.setSelectedIndex(0);
        sagUstPanel.add(lblFiltre);
        sagUstPanel.add(cmbKullaniciTipi);
        
        ustPanel.add(solUstPanel, BorderLayout.WEST);
        ustPanel.add(sagUstPanel, BorderLayout.EAST);
        
        // Tablo - Kullanıcılar
        String[] kolonlar = {"ID", "Kullanıcı Adı", "Ad", "Soyad", "E-posta", "Rol"};
        tableModel = new DefaultTableModel(kolonlar, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tblKullanicilar = new JTable(tableModel);
        tblKullanicilar.getTableHeader().setReorderingAllowed(false);
        tblKullanicilar.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        JScrollPane scrollPane = new JScrollPane(tblKullanicilar);
        scrollPane.setPreferredSize(new Dimension(850, 400));
        
        // Butonlar Paneli
        JPanel butonlarPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        
        btnYeniKullanici = new JButton("Yeni Kullanıcı Ekle");
        btnKullaniciDuzenle = new JButton("Kullanıcıyı Düzenle");
        btnKullaniciSil = new JButton("Kullanıcıyı Sil");
        btnYenile = new JButton("Yenile");
        btnCikis = new JButton("Çıkış");
        
        butonlarPanel.add(btnYeniKullanici);
        butonlarPanel.add(btnKullaniciDuzenle);
        butonlarPanel.add(btnKullaniciSil);
        butonlarPanel.add(btnYenile);
        butonlarPanel.add(btnCikis);
        
        // Panelleri ana panele ekle
        panel.add(ustPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(butonlarPanel, BorderLayout.SOUTH);
        
        getContentPane().add(panel);
    }
    
    private void setupListeners() {
        cmbKullaniciTipi.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                kullanicilariYukle();
            }
        });
        
        btnYeniKullanici.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                yeniKullaniciEkle();
            }
        });
        
        btnKullaniciDuzenle.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                secilenKullaniciyiDuzenle();
            }
        });
        
        btnKullaniciSil.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                secilenKullaniciyiSil();
            }
        });
        
        btnYenile.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                kullanicilariYukle();
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
     * Kullanıcıları yükler ve tabloya ekler
     */
    private void kullanicilariYukle() {
        try {
            // Tabloyu temizle
            tableModel.setRowCount(0);
            
            // Kullanıcıları getir (filtre uygula)
            List<Kullanici> kullanicilar;
            int seciliFiltre = cmbKullaniciTipi.getSelectedIndex();
            
            if (seciliFiltre == 0) {
                // Tümü
                kullanicilar = kullaniciDAO.tumKullanicilariGetir();
            } else {
                // Belirli rol
                Kullanici.Rol rol = Kullanici.Rol.values()[seciliFiltre - 1];
                kullanicilar = kullaniciDAO.getRoleGoreKullanicilar(rol);
            }
            
            // Tabloyu doldur
            for (Kullanici kullanici : kullanicilar) {
                Object[] satir = {
                    kullanici.getId(),
                    kullanici.getKullaniciAdi(),
                    kullanici.getAd(),
                    kullanici.getSoyad(),
                    kullanici.getEmail(),
                    kullanici.getRol().toString()
                };
                
                tableModel.addRow(satir);
            }
            
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, 
                "Kullanıcılar yüklenirken hata oluştu: " + ex.getMessage(), 
                "Hata", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }
    
    /**
     * Yeni kullanıcı ekleme işlemi
     */
    private void yeniKullaniciEkle() {
        // Kullanıcı bilgilerini al
        JTextField txtKullaniciAdi = new JTextField();
        JTextField txtSifre = new JTextField();
        JTextField txtAd = new JTextField();
        JTextField txtSoyad = new JTextField();
        JTextField txtEmail = new JTextField();
        JComboBox<String> cmbRol = new JComboBox<>(new String[]{"Öğrenci", "Öğretim Üyesi", "Admin"});
        
        Object[] inputs = {
            "Kullanıcı Adı:", txtKullaniciAdi,
            "Şifre:", txtSifre,
            "Ad:", txtAd,
            "Soyad:", txtSoyad,
            "E-posta:", txtEmail,
            "Rol:", cmbRol
        };
        
        int option = JOptionPane.showConfirmDialog(this, inputs, "Yeni Kullanıcı Ekle", JOptionPane.OK_CANCEL_OPTION);
        
        if (option == JOptionPane.OK_OPTION) {
            // Alanların boş olup olmadığını kontrol et
            if (txtKullaniciAdi.getText().isEmpty() || txtSifre.getText().isEmpty() ||
                txtAd.getText().isEmpty() || txtSoyad.getText().isEmpty() || txtEmail.getText().isEmpty()) {
                JOptionPane.showMessageDialog(this, 
                    "Lütfen tüm alanları doldurunuz.", 
                    "Uyarı", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            // Şifre güvenliğini kontrol et
            if (!PasswordUtil.isStrongPassword(txtSifre.getText())) {
                JOptionPane.showMessageDialog(this, 
                    "Şifre yeterince güçlü değil! Şifre en az 8 karakter olmalı ve büyük harf, küçük harf, rakam ve özel karakter içermelidir.", 
                    "Uyarı", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            try {
                // Yeni kullanıcı oluştur
                Kullanici yeniKullanici = new Kullanici();
                yeniKullanici.setKullaniciAdi(txtKullaniciAdi.getText());
                yeniKullanici.setSifre(txtSifre.getText()); // Gerçek uygulamada şifre hash'lenmeli
                yeniKullanici.setAd(txtAd.getText());
                yeniKullanici.setSoyad(txtSoyad.getText());
                yeniKullanici.setEmail(txtEmail.getText());
                
                // Rolü ayarla
                int selectedRol = cmbRol.getSelectedIndex();
                Kullanici.Rol rol = Kullanici.Rol.values()[selectedRol];
                yeniKullanici.setRol(rol);
                
                // Veritabanına ekle
                int id = kullaniciDAO.ekle(yeniKullanici);
                
                if (id > 0) {
                    JOptionPane.showMessageDialog(this, 
                        "Kullanıcı başarıyla eklendi.", 
                        "İşlem Başarılı", JOptionPane.INFORMATION_MESSAGE);
                    
                    // Tabloyu yenile
                    kullanicilariYukle();
                } else {
                    JOptionPane.showMessageDialog(this, 
                        "Kullanıcı eklenirken bir hata oluştu.", 
                        "Hata", JOptionPane.ERROR_MESSAGE);
                }
                
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, 
                    "Kullanıcı eklenirken hata oluştu: " + ex.getMessage(), 
                    "Hata", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        }
    }
    
    /**
     * Seçilen kullanıcıyı düzenleme işlemi
     */
    private void secilenKullaniciyiDuzenle() {
        int selectedRow = tblKullanicilar.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, 
                "Lütfen düzenlemek istediğiniz kullanıcıyı seçin.", 
                "Uyarı", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int kullaniciId = (int) tableModel.getValueAt(selectedRow, 0);
        
        try {
            // Kullanıcıyı veritabanından getir
            Kullanici kullanici = kullaniciDAO.getirById(kullaniciId);
            
            if (kullanici == null) {
                JOptionPane.showMessageDialog(this, 
                    "Kullanıcı bulunamadı.", 
                    "Hata", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Kullanıcı bilgilerini göster ve düzenleme için hazırla
            JTextField txtKullaniciAdi = new JTextField(kullanici.getKullaniciAdi());
            JTextField txtSifre = new JTextField(""); // Şifre alanını boş bırak
            JTextField txtAd = new JTextField(kullanici.getAd());
            JTextField txtSoyad = new JTextField(kullanici.getSoyad());
            JTextField txtEmail = new JTextField(kullanici.getEmail());
            
            JComboBox<String> cmbRol = new JComboBox<>(new String[]{"Öğrenci", "Öğretim Üyesi", "Admin"});
            cmbRol.setSelectedIndex(kullanici.getRol().ordinal());
            
            JCheckBox chkSifreDegistir = new JCheckBox("Şifreyi değiştir");
            txtSifre.setEnabled(false);
            
            chkSifreDegistir.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    txtSifre.setEnabled(chkSifreDegistir.isSelected());
                }
            });
            
            Object[] inputs = {
                "Kullanıcı Adı:", txtKullaniciAdi,
                chkSifreDegistir,
                "Yeni Şifre:", txtSifre,
                "Ad:", txtAd,
                "Soyad:", txtSoyad,
                "E-posta:", txtEmail,
                "Rol:", cmbRol
            };
            
            int option = JOptionPane.showConfirmDialog(this, inputs, "Kullanıcıyı Düzenle", JOptionPane.OK_CANCEL_OPTION);
            
            if (option == JOptionPane.OK_OPTION) {
                // Alanların boş olup olmadığını kontrol et
                if (txtKullaniciAdi.getText().isEmpty() || 
                    txtAd.getText().isEmpty() || txtSoyad.getText().isEmpty() || txtEmail.getText().isEmpty()) {
                    JOptionPane.showMessageDialog(this, 
                        "Lütfen tüm alanları doldurunuz (şifre hariç).", 
                        "Uyarı", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                
                // Şifre değiştirilecekse ve yeterince güçlü değilse uyarı ver
                if (chkSifreDegistir.isSelected() && !txtSifre.getText().isEmpty() && 
                    !PasswordUtil.isStrongPassword(txtSifre.getText())) {
                    JOptionPane.showMessageDialog(this, 
                        "Şifre yeterince güçlü değil! Şifre en az 8 karakter olmalı ve büyük harf, küçük harf, rakam ve özel karakter içermelidir.", 
                        "Uyarı", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                
                // Kullanıcı bilgilerini güncelle
                kullanici.setKullaniciAdi(txtKullaniciAdi.getText());
                if (chkSifreDegistir.isSelected() && !txtSifre.getText().isEmpty()) {
                    kullanici.setSifre(txtSifre.getText()); // Gerçek uygulamada şifre hash'lenmeli
                }
                kullanici.setAd(txtAd.getText());
                kullanici.setSoyad(txtSoyad.getText());
                kullanici.setEmail(txtEmail.getText());
                
                // Rolü güncelle
                int selectedRol = cmbRol.getSelectedIndex();
                Kullanici.Rol rol = Kullanici.Rol.values()[selectedRol];
                kullanici.setRol(rol);
                
                // Veritabanında güncelle
                boolean sonuc = kullaniciDAO.guncelle(kullanici);
                
                if (sonuc) {
                    JOptionPane.showMessageDialog(this, 
                        "Kullanıcı başarıyla güncellendi.", 
                        "İşlem Başarılı", JOptionPane.INFORMATION_MESSAGE);
                    
                    // Tabloyu yenile
                    kullanicilariYukle();
                } else {
                    JOptionPane.showMessageDialog(this, 
                        "Kullanıcı güncellenirken bir hata oluştu.", 
                        "Hata", JOptionPane.ERROR_MESSAGE);
                }
            }
            
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, 
                "Kullanıcı işlemi sırasında hata oluştu: " + ex.getMessage(), 
                "Hata", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }
    
    /**
     * Seçilen kullanıcıyı silme işlemi
     */
    private void secilenKullaniciyiSil() {
        int selectedRow = tblKullanicilar.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, 
                "Lütfen silmek istediğiniz kullanıcıyı seçin.", 
                "Uyarı", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int kullaniciId = (int) tableModel.getValueAt(selectedRow, 0);
        String kullaniciAdi = (String) tableModel.getValueAt(selectedRow, 1);
        
        // Kendisini silmek isterse uyarı ver
        if (kullaniciId == admin.getId()) {
            JOptionPane.showMessageDialog(this, 
                "Kendi hesabınızı silemezsiniz!", 
                "Uyarı", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Onay iste
        int secim = JOptionPane.showConfirmDialog(this, 
            "\"" + kullaniciAdi + "\" kullanıcısını silmek istediğinize emin misiniz?\n" +
            "Bu işlem geri alınamaz ve kullanıcıya ait tüm veriler silinecektir.", 
            "Kullanıcı Silme", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        
        if (secim == JOptionPane.YES_OPTION) {
            try {
                // Kullanıcıyı sil
                boolean sonuc = kullaniciDAO.sil(kullaniciId);
                
                if (sonuc) {
                    JOptionPane.showMessageDialog(this, 
                        "Kullanıcı başarıyla silindi.", 
                        "İşlem Başarılı", JOptionPane.INFORMATION_MESSAGE);
                    
                    // Tabloyu yenile
                    kullanicilariYukle();
                } else {
                    JOptionPane.showMessageDialog(this, 
                        "Kullanıcı silinirken bir hata oluştu.", 
                        "Hata", JOptionPane.ERROR_MESSAGE);
                }
                
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, 
                    "Kullanıcı silinirken hata oluştu: " + ex.getMessage(), 
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