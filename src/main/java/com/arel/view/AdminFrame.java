package com.arel.view;

import com.arel.database.KullaniciDAO;
import com.arel.database.RandevuDAO;
import com.arel.model.Kullanici;
import com.arel.util.PasswordUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
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
    
    // Renk şeması
    private static final Color PRIMARY_COLOR = new Color(41, 128, 185); // Mavi
    private static final Color SECONDARY_COLOR = new Color(52, 152, 219); // Açık mavi
    private static final Color BACKGROUND_COLOR = new Color(245, 245, 245); // Açık gri
    private static final Color TEXT_COLOR = new Color(44, 62, 80); // Koyu lacivert
    private static final Color BUTTON_COLOR = new Color(52, 152, 219); // Açık mavi 
    private static final Color BUTTON_TEXT_COLOR = Color.WHITE;
    private static final Color SUCCESS_COLOR = new Color(46, 204, 113); // Yeşil
    private static final Color DANGER_COLOR = new Color(231, 76, 60); // Kırmızı
    
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
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(BACKGROUND_COLOR);
        
        // Ana panel
        JPanel mainPanel = new JPanel(new BorderLayout(0, 0));
        mainPanel.setBackground(BACKGROUND_COLOR);
        mainPanel.setBorder(new EmptyBorder(0, 0, 0, 0));
        
        // Header Panel
        JPanel headerPanel = createHeaderPanel();
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        
        // Content Panel
        JPanel contentPanel = createContentPanel();
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        
        setContentPane(mainPanel);
    }
    
    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BorderLayout());
        headerPanel.setBackground(PRIMARY_COLOR);
        headerPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        
        // Başlık ve Admin Bilgisi
        JLabel lblHosgeldiniz = new JLabel("Hoş geldiniz, " + admin.getTamAd());
        lblHosgeldiniz.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblHosgeldiniz.setForeground(Color.WHITE);
        
        JLabel lblRol = new JLabel("Sistem Yöneticisi");
        lblRol.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblRol.setForeground(new Color(255, 255, 255, 200));
        
        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setBackground(PRIMARY_COLOR);
        textPanel.add(lblHosgeldiniz);
        textPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        textPanel.add(lblRol);
        
        // Sağ tarafta filtre
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        filterPanel.setBackground(PRIMARY_COLOR);
        
        JLabel lblFiltre = new JLabel("Kullanıcı Tipi:");
        lblFiltre.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblFiltre.setForeground(Color.WHITE);
        
        cmbKullaniciTipi = new JComboBox<>(new String[]{"Tümü", "Öğrenci", "Öğretim Üyesi", "Admin"});
        cmbKullaniciTipi.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cmbKullaniciTipi.setBackground(Color.WHITE);
        cmbKullaniciTipi.setPreferredSize(new Dimension(150, 30));
        
        filterPanel.add(lblFiltre);
        filterPanel.add(cmbKullaniciTipi);
        
        headerPanel.add(textPanel, BorderLayout.WEST);
        headerPanel.add(filterPanel, BorderLayout.EAST);
        
        return headerPanel;
    }
    
    private JPanel createContentPanel() {
        JPanel contentPanel = new JPanel(new BorderLayout(15, 15));
        contentPanel.setBackground(BACKGROUND_COLOR);
        contentPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        // Tablo Başlığı
        JLabel lblKullanicilar = new JLabel("Sistem Kullanıcıları");
        lblKullanicilar.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblKullanicilar.setForeground(TEXT_COLOR);
        
        // Tablo - Kullanıcılar
        String[] kolonlar = {"ID", "Kullanıcı Adı", "Ad", "Soyad", "E-posta", "Rol"};
        tableModel = new DefaultTableModel(kolonlar, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tblKullanicilar = new JTable(tableModel);
        tblKullanicilar.setRowHeight(30);
        tblKullanicilar.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tblKullanicilar.setSelectionBackground(SECONDARY_COLOR);
        tblKullanicilar.setSelectionForeground(Color.WHITE);
        tblKullanicilar.setShowGrid(false);
        tblKullanicilar.setIntercellSpacing(new Dimension(0, 0));
        tblKullanicilar.getTableHeader().setReorderingAllowed(false);
        tblKullanicilar.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // Tablo başlık ayarları
        JTableHeader header = tblKullanicilar.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));
        header.setBackground(SECONDARY_COLOR);
        header.setForeground(Color.WHITE);
        header.setPreferredSize(new Dimension(header.getWidth(), 35));
        
        JScrollPane scrollPane = new JScrollPane(tblKullanicilar);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Color.WHITE);
        
        // Butonlar Paneli
        JPanel butonlarPanel = createButtonsPanel();
        
        // Panelleri içerik paneline ekle
        contentPanel.add(lblKullanicilar, BorderLayout.NORTH);
        contentPanel.add(scrollPane, BorderLayout.CENTER);
        contentPanel.add(butonlarPanel, BorderLayout.SOUTH);
        
        return contentPanel;
    }
    
    private JPanel createButtonsPanel() {
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        buttonsPanel.setBackground(BACKGROUND_COLOR);
        buttonsPanel.setBorder(new EmptyBorder(15, 0, 0, 0));
        
        btnYeniKullanici = createStyledButton("Yeni Kullanıcı Ekle", SUCCESS_COLOR, BUTTON_TEXT_COLOR);
        btnKullaniciDuzenle = createStyledButton("Kullanıcıyı Düzenle", BUTTON_COLOR, BUTTON_TEXT_COLOR);
        btnKullaniciSil = createStyledButton("Kullanıcıyı Sil", DANGER_COLOR, BUTTON_TEXT_COLOR);
        btnYenile = createStyledButton("Yenile", new Color(52, 152, 219), BUTTON_TEXT_COLOR);
        btnCikis = createStyledButton("Çıkış", new Color(149, 165, 166), BUTTON_TEXT_COLOR);
        
        buttonsPanel.add(btnYeniKullanici);
        buttonsPanel.add(btnKullaniciDuzenle);
        buttonsPanel.add(btnKullaniciSil);
        buttonsPanel.add(btnYenile);
        buttonsPanel.add(btnCikis);
        
        return buttonsPanel;
    }
    
    private JButton createStyledButton(String text, Color bgColor, Color fgColor) {
        JButton button = new JButton(text);
        button.setBackground(bgColor);
        button.setForeground(fgColor);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(180, 40));
        
        return button;
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
            showErrorMessage("Kullanıcılar yüklenirken hata oluştu: " + ex.getMessage());
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
        
        stylizeInputField(txtKullaniciAdi);
        stylizeInputField(txtSifre);
        stylizeInputField(txtAd);
        stylizeInputField(txtSoyad);
        stylizeInputField(txtEmail);
        stylizeComboBox(cmbRol);
        
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(6, 2, 10, 10));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        panel.add(createBoldLabel("Kullanıcı Adı:"));
        panel.add(txtKullaniciAdi);
        panel.add(createBoldLabel("Şifre:"));
        panel.add(txtSifre);
        panel.add(createBoldLabel("Ad:"));
        panel.add(txtAd);
        panel.add(createBoldLabel("Soyad:"));
        panel.add(txtSoyad);
        panel.add(createBoldLabel("E-posta:"));
        panel.add(txtEmail);
        panel.add(createBoldLabel("Rol:"));
        panel.add(cmbRol);
        
        int option = JOptionPane.showConfirmDialog(this, panel, "Yeni Kullanıcı Ekle", 
                                                  JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        
        if (option == JOptionPane.OK_OPTION) {
            // Alanların boş olup olmadığını kontrol et
            if (txtKullaniciAdi.getText().isEmpty() || txtSifre.getText().isEmpty() ||
                txtAd.getText().isEmpty() || txtSoyad.getText().isEmpty() || txtEmail.getText().isEmpty()) {
                showWarningMessage("Lütfen tüm alanları doldurunuz.");
                return;
            }
            
            // Şifre güvenliğini kontrol et
            if (!PasswordUtil.isStrongPassword(txtSifre.getText())) {
                showWarningMessage("Şifre yeterince güçlü değil! Şifre en az 8 karakter olmalı ve büyük harf, küçük harf, rakam ve özel karakter içermelidir.");
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
                    showInfoMessage("Kullanıcı başarıyla eklendi.");
                    
                    // Tabloyu yenile
                    kullanicilariYukle();
                } else {
                    showErrorMessage("Kullanıcı eklenirken bir hata oluştu.");
                }
                
            } catch (SQLException ex) {
                showErrorMessage("Kullanıcı eklenirken hata oluştu: " + ex.getMessage());
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
            showWarningMessage("Lütfen düzenlemek istediğiniz kullanıcıyı seçin.");
            return;
        }
        
        int kullaniciId = (int) tableModel.getValueAt(selectedRow, 0);
        
        try {
            // Kullanıcıyı veritabanından getir
            Kullanici kullanici = kullaniciDAO.getirById(kullaniciId);
            
            if (kullanici == null) {
                showErrorMessage("Kullanıcı bulunamadı.");
                return;
            }
            
            // Kullanıcı bilgilerini göster ve düzenleme için hazırla
            JTextField txtKullaniciAdi = new JTextField(kullanici.getKullaniciAdi());
            JTextField txtSifre = new JTextField("");
            JTextField txtAd = new JTextField(kullanici.getAd());
            JTextField txtSoyad = new JTextField(kullanici.getSoyad());
            JTextField txtEmail = new JTextField(kullanici.getEmail());
            
            stylizeInputField(txtKullaniciAdi);
            stylizeInputField(txtSifre);
            stylizeInputField(txtAd);
            stylizeInputField(txtSoyad);
            stylizeInputField(txtEmail);
            
            JComboBox<String> cmbRol = new JComboBox<>(new String[]{"Öğrenci", "Öğretim Üyesi", "Admin"});
            cmbRol.setSelectedIndex(kullanici.getRol().ordinal());
            stylizeComboBox(cmbRol);
            
            JCheckBox chkSifreDegistir = new JCheckBox("Şifreyi değiştir");
            chkSifreDegistir.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            txtSifre.setEnabled(false);
            
            chkSifreDegistir.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    txtSifre.setEnabled(chkSifreDegistir.isSelected());
                }
            });
            
            JPanel panel = new JPanel();
            panel.setLayout(new GridLayout(7, 2, 10, 10));
            panel.setBorder(new EmptyBorder(10, 10, 10, 10));
            
            panel.add(createBoldLabel("Kullanıcı Adı:"));
            panel.add(txtKullaniciAdi);
            panel.add(chkSifreDegistir);
            panel.add(txtSifre);
            panel.add(createBoldLabel("Ad:"));
            panel.add(txtAd);
            panel.add(createBoldLabel("Soyad:"));
            panel.add(txtSoyad);
            panel.add(createBoldLabel("E-posta:"));
            panel.add(txtEmail);
            panel.add(createBoldLabel("Rol:"));
            panel.add(cmbRol);
            
            int option = JOptionPane.showConfirmDialog(this, panel, "Kullanıcıyı Düzenle", 
                                                      JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            
            if (option == JOptionPane.OK_OPTION) {
                // Alanların boş olup olmadığını kontrol et
                if (txtKullaniciAdi.getText().isEmpty() || 
                    txtAd.getText().isEmpty() || txtSoyad.getText().isEmpty() || txtEmail.getText().isEmpty()) {
                    showWarningMessage("Lütfen tüm alanları doldurunuz (şifre hariç).");
                    return;
                }
                
                // Şifre değiştirilecekse ve yeterince güçlü değilse uyarı ver
                if (chkSifreDegistir.isSelected() && !txtSifre.getText().isEmpty() && 
                    !PasswordUtil.isStrongPassword(txtSifre.getText())) {
                    showWarningMessage("Şifre yeterince güçlü değil! Şifre en az 8 karakter olmalı ve büyük harf, küçük harf, rakam ve özel karakter içermelidir.");
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
                    showInfoMessage("Kullanıcı başarıyla güncellendi.");
                    
                    // Tabloyu yenile
                    kullanicilariYukle();
                } else {
                    showErrorMessage("Kullanıcı güncellenirken bir hata oluştu.");
                }
            }
            
        } catch (SQLException ex) {
            showErrorMessage("Kullanıcı işlemi sırasında hata oluştu: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
    
    /**
     * Seçilen kullanıcıyı silme işlemi
     */
    private void secilenKullaniciyiSil() {
        int selectedRow = tblKullanicilar.getSelectedRow();
        if (selectedRow == -1) {
            showWarningMessage("Lütfen silmek istediğiniz kullanıcıyı seçin.");
            return;
        }
        
        int kullaniciId = (int) tableModel.getValueAt(selectedRow, 0);
        String kullaniciAdi = (String) tableModel.getValueAt(selectedRow, 1);
        
        // Kendisini silmek isterse uyarı ver
        if (kullaniciId == admin.getId()) {
            showWarningMessage("Kendi hesabınızı silemezsiniz!");
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
                    showInfoMessage("Kullanıcı başarıyla silindi.");
                    
                    // Tabloyu yenile
                    kullanicilariYukle();
                } else {
                    showErrorMessage("Kullanıcı silinirken bir hata oluştu.");
                }
                
            } catch (SQLException ex) {
                showErrorMessage("Kullanıcı silinirken hata oluştu: " + ex.getMessage());
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
            "Çıkış", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        
        if (secim == JOptionPane.YES_OPTION) {
            dispose();
            new LoginFrame().setVisible(true);
        }
    }
    
    // Yardımcı metodlar
    private JLabel createBoldLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 14));
        return label;
    }
    
    private void stylizeInputField(JTextField textField) {
        textField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        textField.setMargin(new Insets(5, 5, 5, 5));
    }
    
    private void stylizeComboBox(JComboBox<String> comboBox) {
        comboBox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        comboBox.setBackground(Color.WHITE);
    }
    
    private void showErrorMessage(String message) {
        JOptionPane.showMessageDialog(this, message, "Hata", JOptionPane.ERROR_MESSAGE);
    }
    
    private void showWarningMessage(String message) {
        JOptionPane.showMessageDialog(this, message, "Uyarı", JOptionPane.WARNING_MESSAGE);
    }
    
    private void showInfoMessage(String message) {
        JOptionPane.showMessageDialog(this, message, "Bilgi", JOptionPane.INFORMATION_MESSAGE);
    }
} 